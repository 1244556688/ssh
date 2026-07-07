package com.example.ssh

import com.example.data.SshHost
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Properties

sealed interface ConnectionState {
    object Idle : ConnectionState
    object Connecting : ConnectionState
    object Connected : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class SftpFileItem(
    val name: String,
    val size: Long,
    val isDirectory: Boolean,
    val permissions: String,
    val mtimeString: String
)

class SshConnectionManager {
    private val jsch = JSch()
    private var session: Session? = null
    private var shellChannel: ChannelShell? = null
    private var shellInput: InputStream? = null
    private var shellOutput: OutputStream? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _terminalOutput = MutableStateFlow<String>("")
    val terminalOutput: StateFlow<String> = _terminalOutput.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var readerJob: Job? = null

    /**
     * Establishes an SSH connection with the server.
     */
    suspend fun connect(host: SshHost): Boolean = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.Connecting
        _terminalOutput.value = ""
        try {
            disconnect()

            val newSession = jsch.getSession(host.username, host.ip, host.port)
            if (host.authType == "PASSWORD") {
                newSession.setPassword(host.password)
            } else {
                // Add private key
                jsch.removeAllIdentity()
                val prvKeyBytes = host.privateKey.toByteArray(Charsets.UTF_8)
                val passphraseBytes = if (host.passphrase.isNotEmpty()) host.passphrase.toByteArray(Charsets.UTF_8) else null
                jsch.addIdentity(host.alias, prvKeyBytes, null, passphraseBytes)
            }

            val config = Properties()
            config["StrictHostKeyChecking"] = "no" // Automatic fallback for ease of use
            newSession.setConfig(config)
            newSession.timeout = host.timeout * 1000
            
            if (host.keepAlive > 0) {
                newSession.serverAliveInterval = host.keepAlive * 1000
            }

            newSession.connect()
            session = newSession

            // Open interactive shell
            val channel = newSession.openChannel("shell") as ChannelShell
            shellChannel = channel
            shellInput = channel.inputStream
            shellOutput = channel.outputStream
            channel.connect()

            _connectionState.value = ConnectionState.Connected
            startTerminalReader()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = ConnectionState.Error(e.localizedMessage ?: "Connection failed")
            false
        }
    }

    /**
     * Terminate the connection and clean up streams.
     */
    fun disconnect() {
        readerJob?.cancel()
        readerJob = null
        try {
            shellChannel?.disconnect()
        } catch (e: Exception) { e.printStackTrace() }
        shellChannel = null
        shellInput = null
        shellOutput = null

        try {
            session?.disconnect()
        } catch (e: Exception) { e.printStackTrace() }
        session = null

        _connectionState.value = ConnectionState.Idle
    }

    /**
     * Writes raw bytes/keys directly into the shell's standard input.
     */
    fun sendInput(input: String) {
        scope.launch(Dispatchers.IO) {
            try {
                shellOutput?.let {
                    it.write(input.toByteArray(Charsets.UTF_8))
                    it.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Launch dynamic terminal background reader
     */
    private fun startTerminalReader() {
        readerJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (shellInput != null) {
                try {
                    bytesRead = shellInput?.read(buffer) ?: -1
                    if (bytesRead == -1) break
                    if (bytesRead > 0) {
                        val text = String(buffer, 0, bytesRead, Charsets.UTF_8)
                        // Append text to terminal output flow
                        _terminalOutput.value += text
                        // Limit buffer length to prevent memory leaks
                        if (_terminalOutput.value.length > 50000) {
                            _terminalOutput.value = _terminalOutput.value.takeLast(25000)
                        }
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    /**
     * Execute a single command synchronously and return output (e.g. for System Monitor)
     */
    suspend fun executeCommand(command: String): String = withContext(Dispatchers.IO) {
        val currentSession = session ?: return@withContext "Error: Not connected"
        try {
            val channel = currentSession.openChannel("exec") as com.jcraft.jsch.ChannelExec
            channel.setCommand(command)
            val outputStream = ByteArrayOutputStream()
            val errorStream = ByteArrayOutputStream()
            channel.setOutputStream(outputStream)
            channel.setErrStream(errorStream)
            
            channel.connect()
            while (!channel.isClosed) {
                Thread.sleep(50)
            }
            channel.disconnect()
            
            val result = outputStream.toString("UTF-8")
            if (result.isEmpty()) {
                errorStream.toString("UTF-8")
            } else {
                result
            }
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    /**
     * Lists directory files over SFTP.
     */
    suspend fun listSftpDirectory(path: String): List<SftpFileItem> = withContext(Dispatchers.IO) {
        val currentSession = session ?: return@withContext emptyList()
        var sftpChannel: ChannelSftp? = null
        try {
            sftpChannel = currentSession.openChannel("sftp") as ChannelSftp
            sftpChannel.connect()
            val list = mutableListOf<SftpFileItem>()
            val vector = sftpChannel.ls(path)
            for (entryObj in vector) {
                val entry = entryObj as? ChannelSftp.LsEntry ?: continue
                val name = entry.filename
                if (name == "." || name == "..") continue
                val attrs = entry.attrs
                list.add(
                    SftpFileItem(
                        name = name,
                        size = attrs.size,
                        isDirectory = attrs.isDir,
                        permissions = attrs.permissionsString,
                        mtimeString = attrs.mtimeString
                    )
                )
            }
            sftpChannel.disconnect()
            list.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        } catch (e: Exception) {
            e.printStackTrace()
            sftpChannel?.disconnect()
            emptyList()
        }
    }

    /**
     * Deletes a file or directory over SFTP.
     */
    suspend fun deleteSftpItem(path: String, isDirectory: Boolean): Boolean = withContext(Dispatchers.IO) {
        val currentSession = session ?: return@withContext false
        var sftpChannel: ChannelSftp? = null
        try {
            sftpChannel = currentSession.openChannel("sftp") as ChannelSftp
            sftpChannel.connect()
            if (isDirectory) {
                sftpChannel.rmdir(path)
            } else {
                sftpChannel.rm(path)
            }
            sftpChannel.disconnect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            sftpChannel?.disconnect()
            false
        }
    }

    /**
     * Creates a directory over SFTP.
     */
    suspend fun makeSftpDirectory(path: String): Boolean = withContext(Dispatchers.IO) {
        val currentSession = session ?: return@withContext false
        var sftpChannel: ChannelSftp? = null
        try {
            sftpChannel = currentSession.openChannel("sftp") as ChannelSftp
            sftpChannel.connect()
            sftpChannel.mkdir(path)
            sftpChannel.disconnect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            sftpChannel?.disconnect()
            false
        }
    }

    /**
     * Renames a file or folder over SFTP.
     */
    suspend fun renameSftpItem(oldPath: String, newPath: String): Boolean = withContext(Dispatchers.IO) {
        val currentSession = session ?: return@withContext false
        var sftpChannel: ChannelSftp? = null
        try {
            sftpChannel = currentSession.openChannel("sftp") as ChannelSftp
            sftpChannel.connect()
            sftpChannel.rename(oldPath, newPath)
            sftpChannel.disconnect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            sftpChannel?.disconnect()
            false
        }
    }
}

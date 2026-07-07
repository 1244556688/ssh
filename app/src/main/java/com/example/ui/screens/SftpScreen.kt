package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ssh.SftpFileItem
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoftWhite
import com.example.ui.viewmodel.SshViewModel

@Composable
fun SftpScreen(
    viewModel: SshViewModel
) {
    val currentPath by viewModel.currentSftpPath.collectAsState()
    val files by viewModel.sftpFiles.collectAsState()
    val isLoading by viewModel.sftpLoading.collectAsState()
    val context = LocalContext.current

    var showCreateDirDialog by remember { mutableStateOf(false) }
    var dirNameToCreate by remember { mutableStateOf("") }

    var fileToRename by remember { mutableStateOf<SftpFileItem?>(null) }
    var newRenameName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SFTP 檔案管理",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftWhite
                    )
                    Text(
                        text = "路徑: $currentPath",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(onClick = { viewModel.loadSftpDirectory(currentPath) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Folder", tint = SoftWhite)
                    }
                    IconButton(onClick = { showCreateDirDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Directory", tint = NeonCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Upper directory navigation breadcrumb button
            if (currentPath != "/") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val upperPath = getParentDirectory(currentPath)
                            viewModel.loadSftpDirectory(upperPath)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Parent Directory", tint = NeonPurple)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("返回上一層目錄 (..)", style = MaterialTheme.typography.bodyMedium, color = NeonPurple, fontWeight = FontWeight.Bold)
                }
            }

            // Folder Files List Content
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonPurple)
                }
            } else if (files.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("此目錄沒有檔案", color = SoftGray, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files) { item ->
                        SftpFileRow(
                            item = item,
                            onClick = {
                                if (item.isDirectory) {
                                    val newPath = if (currentPath.endsWith("/")) "$currentPath${item.name}" else "$currentPath/${item.name}"
                                    viewModel.loadSftpDirectory(newPath)
                                } else {
                                    Toast.makeText(context, "下載檔案: ${item.name}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onRename = {
                                fileToRename = item
                                newRenameName = item.name
                            },
                            onDelete = {
                                viewModel.deleteSftpItem(item)
                                Toast.makeText(context, "刪除: ${item.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Create Directory Dialog
    if (showCreateDirDialog) {
        Dialog(onDismissRequest = { showCreateDirDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFF131524),
                modifier = Modifier.padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("新增資料夾", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                    OutlinedTextField(
                        value = dirNameToCreate,
                        onValueChange = { dirNameToCreate = it },
                        placeholder = { Text("資料夾名稱...") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateDirDialog = false }) {
                            Text("取消", color = SoftGray)
                        }
                        Button(
                            onClick = {
                                if (dirNameToCreate.isNotEmpty()) {
                                    viewModel.createSftpDirectory(dirNameToCreate)
                                    dirNameToCreate = ""
                                    showCreateDirDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Text("建立")
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    fileToRename?.let { item ->
        Dialog(onDismissRequest = { fileToRename = null }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFF131524),
                modifier = Modifier.padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("重命名檔案 / 資料夾", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                    OutlinedTextField(
                        value = newRenameName,
                        onValueChange = { newRenameName = it },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { fileToRename = null }) {
                            Text("取消", color = SoftGray)
                        }
                        Button(
                            onClick = {
                                if (newRenameName.isNotEmpty() && newRenameName != item.name) {
                                    viewModel.renameSftpItem(item, newRenameName)
                                    fileToRename = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Text("修改")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SftpFileRow(
    item: SftpFileItem,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("sftp_item_${item.name}"),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (item.isDirectory) Icons.Default.FolderOpen else Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = if (item.isDirectory) NeonPurple else NeonCyan,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (item.isDirectory) "資料夾 | ${item.permissions}" else "${formatFileSize(item.size)} | ${item.permissions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftGray
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DriveFileRenameOutline, contentDescription = "Rename file", tint = NeonCyan, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete file", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun getParentDirectory(path: String): String {
    if (path == "/") return "/"
    val lastSlash = path.lastIndexOf("/")
    if (lastSlash <= 0) return "/"
    return path.substring(0, lastSlash)
}

private fun formatFileSize(size: Long): String {
    if (size < 1024) return "$size B"
    val kb = size / 1024
    if (kb < 1024) return "$kb KB"
    val mb = kb / 1024
    return "$mb MB"
}

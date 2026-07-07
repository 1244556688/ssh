package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ActiveSessionContainer
import com.example.ui.screens.BiometricLockScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SshViewModel

/**
 * MainActivity handles edge-to-edge rendering, top-level layout states,
 * and binds directly to the Android Biometric Prompt APIs.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val viewModel: SshViewModel = viewModel()
                    
                    // State trackers
                    var isAppUnlocked by remember { mutableStateOf(false) }
                    var isSshConnected by remember { mutableStateOf(false) }

                    if (!isAppUnlocked) {
                        BiometricLockScreen(
                            onUnlockTrigger = {
                                triggerBiometricPrompt(
                                    onSuccess = {
                                        isAppUnlocked = true
                                        Toast.makeText(this, "驗證解鎖成功", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(this, "驗證失敗: $err", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onBypassUnlock = {
                                isAppUnlocked = true
                                Toast.makeText(this, "測試模式：安全解鎖繞過", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else if (!isSshConnected) {
                        DashboardScreen(
                            viewModel = viewModel,
                            onConnectClick = { host ->
                                viewModel.connectToHost(host)
                                isSshConnected = true
                            }
                        )
                    } else {
                        ActiveSessionContainer(
                            viewModel = viewModel,
                            onDisconnect = {
                                isSshConnected = false
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Spawns standard Android platform biometric authentication dialogs.
     */
    private fun triggerBiometricPrompt(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailure(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailure("Biometric match failed")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("MySSH 安全認證")
            .setSubtitle("請使用生物辨識驗證解鎖設定金鑰")
            .setNegativeButtonText("取消變更")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onFailure(e.localizedMessage ?: "Device authentication unsupported")
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoftWhite
import com.example.ui.viewmodel.SshViewModel

@Composable
fun ActiveSessionContainer(
    viewModel: SshViewModel,
    onDisconnect: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val activeHost by viewModel.activeHost.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .testTag("session_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                    label = { Text("終端機") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray,
                        indicatorColor = Color(0x1BFFFFFF)
                    ),
                    modifier = Modifier.testTag("tab_terminal")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.FolderShared, contentDescription = null) },
                    label = { Text("SFTP 檔案") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray,
                        indicatorColor = Color(0x1BFFFFFF)
                    ),
                    modifier = Modifier.testTag("tab_sftp")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MonitorHeart, contentDescription = null) },
                    label = { Text("監控面板") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray,
                        indicatorColor = Color(0x1BFFFFFF)
                    ),
                    modifier = Modifier.testTag("tab_monitor")
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {
                        viewModel.disconnectSession()
                        onDisconnect()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFEF4444)) },
                    label = { Text("斷開連線", color = Color(0xFFEF4444)) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color(0xFFEF4444),
                        unselectedTextColor = Color(0xFFEF4444)
                    ),
                    modifier = Modifier.testTag("tab_disconnect")
                )
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> TerminalScreen(viewModel = viewModel, onBackClick = {
                    viewModel.disconnectSession()
                    onDisconnect()
                })
                1 -> SftpScreen(viewModel = viewModel)
                2 -> MonitorScreen(viewModel = viewModel)
            }
        }
    }
}

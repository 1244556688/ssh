package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.SshHost
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoftWhite
import com.example.ui.viewmodel.SshViewModel

@Composable
fun DashboardScreen(
    viewModel: SshViewModel,
    onConnectClick: (SshHost) -> Unit
) {
    val hosts by viewModel.savedHosts.collectAsState()
    val latencies by viewModel.hostLatencies.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterBookmarkedOnly by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var hostToEdit by remember { mutableStateOf<SshHost?>(null) }

    val filteredHosts = hosts.filter { host ->
        val matchesSearch = host.alias.contains(searchQuery, ignoreCase = true) || 
                            host.ip.contains(searchQuery, ignoreCase = true)
        val matchesBookmark = !filterBookmarkedOnly || host.isBookmarked
        matchesSearch && matchesBookmark
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonPurple,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_host_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Server Host")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            // Header Banner
            Text(
                text = "MySSH",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = SoftWhite
            )
            Text(
                text = "Secure Terminal Server Shell",
                style = MaterialTheme.typography.bodyLarge,
                color = SoftGray,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Search and Bookmark Filtering Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜尋主機或 IP...", color = SoftGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftGray) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_bar"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SoftWhite,
                        unfocusedTextColor = SoftWhite,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SoftGray.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = { filterBookmarkedOnly = !filterBookmarkedOnly },
                    modifier = Modifier.testTag("filter_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (filterBookmarkedOnly) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Filter Favorites",
                        tint = if (filterBookmarkedOnly) NeonPurple else SoftGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hosts List
            if (filteredHosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = SoftGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || filterBookmarkedOnly) "沒有符合條件的主機" else "尚未儲存任何 SSH 主機",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHosts, key = { it.id }) { host ->
                        val latency = latencies[host.id] ?: "Checking..."
                        HostCard(
                            host = host,
                            latency = latency,
                            onConnect = { onConnectClick(host) },
                            onToggleBookmark = {
                                viewModel.updateHost(host.copy(isBookmarked = !host.isBookmarked))
                            },
                            onEdit = { hostToEdit = host },
                            onDelete = { viewModel.deleteHost(host) }
                        )
                    }
                }
            }
        }
    }

    // Add Host Dialog
    if (showAddDialog) {
        HostEditDialog(
            host = null,
            onDismiss = { showAddDialog = false },
            onSave = { newHost ->
                viewModel.addHost(newHost)
                showAddDialog = false
            }
        )
    }

    // Edit Host Dialog
    hostToEdit?.let { host ->
        HostEditDialog(
            host = host,
            onDismiss = { hostToEdit = null },
            onSave = { updatedHost ->
                viewModel.updateHost(updatedHost)
                hostToEdit = null
            }
        )
    }
}

@Composable
fun HostCard(
    host: SshHost,
    latency: String,
    onConnect: () -> Unit,
    onToggleBookmark: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val latencyColor = when {
        latency == "Offline" -> Color(0xFFEF4444)
        latency.contains("ms") -> NeonGreen
        else -> SoftGray
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("host_card_${host.id}"),
        contentPadding = 16.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = host.alias,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftWhite
                        )
                        Text(
                            text = "${host.username}@${host.ip}:${host.port}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Latency Badge
                    Text(
                        text = latency,
                        style = MaterialTheme.typography.labelSmall,
                        color = latencyColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )

                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (host.isBookmarked) Icons.Default.Star else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorite",
                            tint = if (host.isBookmarked) Color(0xFFFFD700) else SoftGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Host", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Host", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("連線", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun HostEditDialog(
    host: SshHost?,
    onDismiss: () -> Unit,
    onSave: (SshHost) -> Unit
) {
    var alias by remember { mutableStateOf(host?.alias ?: "") }
    var ip by remember { mutableStateOf(host?.ip ?: "") }
    var port by remember { mutableStateOf(host?.port?.toString() ?: "22") }
    var username by remember { mutableStateOf(host?.username ?: "") }
    var authType by remember { mutableStateOf(host?.authType ?: "PASSWORD") }
    var password by remember { mutableStateOf(host?.password ?: "") }
    var privateKey by remember { mutableStateOf(host?.privateKey ?: "") }
    var passphrase by remember { mutableStateOf(host?.passphrase ?: "") }
    var timeout by remember { mutableStateOf(host?.timeout?.toString() ?: "10") }
    var keepAlive by remember { mutableStateOf(host?.keepAlive?.toString() ?: "30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131524)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(1.dp, Color(0x33FFFFFF), MaterialTheme.shapes.large)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (host == null) "新增連線主機" else "修改主機資訊",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SoftWhite
                )

                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("主機別名 (例如: 測試主機)", color = SoftGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        label = { Text("IP / 網域名稱", color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )

                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port", color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("使用者帳號 (Username)", color = SoftGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Auth Type Toggle
                Column {
                    Text("登入認證方式", style = MaterialTheme.typography.bodyMedium, color = SoftWhite)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = authType == "PASSWORD",
                            onClick = { authType = "PASSWORD" },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonPurple)
                        )
                        Text("密碼登入", color = SoftWhite, modifier = Modifier.clickable { authType = "PASSWORD" })

                        Spacer(modifier = Modifier.width(20.dp))

                        RadioButton(
                            selected = authType == "PRIVATE_KEY",
                            onClick = { authType = "PRIVATE_KEY" },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonPurple)
                        )
                        Text("私鑰金鑰 (Key)", color = SoftWhite, modifier = Modifier.clickable { authType = "PRIVATE_KEY" })
                    }
                }

                AnimatedVisibility(visible = authType == "PASSWORD") {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密碼 (Password)", color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedVisibility(visible = authType == "PRIVATE_KEY") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = privateKey,
                            onValueChange = { privateKey = it },
                            label = { Text("私鑰內容 (PEM 格式...)", color = SoftGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 6
                        )

                        OutlinedTextField(
                            value = passphrase,
                            onValueChange = { passphrase = it },
                            label = { Text("私鑰密碼 (Passphrase - 若有)", color = SoftGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = timeout,
                        onValueChange = { timeout = it },
                        label = { Text("逾時 (秒)", color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = keepAlive,
                        onValueChange = { keepAlive = it },
                        label = { Text("保持連線 (秒)", color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftWhite, unfocusedTextColor = SoftWhite, focusedBorderColor = NeonPurple),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = SoftGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (alias.isNotEmpty() && ip.isNotEmpty() && username.isNotEmpty()) {
                                val resolvedPort = port.toIntOrNull() ?: 22
                                val resolvedTimeout = timeout.toIntOrNull() ?: 10
                                val resolvedKeepAlive = keepAlive.toIntOrNull() ?: 30
                                val freshHost = SshHost.create(
                                    id = host?.id ?: 0,
                                    alias = alias,
                                    ip = ip,
                                    port = resolvedPort,
                                    username = username,
                                    authType = authType,
                                    passwordRaw = password,
                                    privateKeyRaw = privateKey,
                                    passphraseRaw = passphrase,
                                    timeout = resolvedTimeout,
                                    keepAlive = resolvedKeepAlive,
                                    isBookmarked = host?.isBookmarked ?: false,
                                    lastConnected = host?.lastConnected ?: 0L
                                )
                                onSave(freshHost)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("儲存")
                    }
                }
            }
        }
    }
}

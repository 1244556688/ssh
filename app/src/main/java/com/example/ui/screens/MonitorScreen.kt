package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CircularGauge
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RealtimeLineChart
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoftWhite
import com.example.ui.viewmodel.SshViewModel

@Composable
fun MonitorScreen(
    viewModel: SshViewModel
) {
    val cpuVal by viewModel.cpuUsage.collectAsState()
    val ramVal by viewModel.ramUsage.collectAsState()
    val diskVal by viewModel.diskUsage.collectAsState()
    val uptime by viewModel.uptime.collectAsState()
    val loadAverage by viewModel.loadAverage.collectAsState()
    val activeHost by viewModel.activeHost.collectAsState()

    // Store historic trends for graphing
    val cpuHistory = remember { mutableStateListOf<Float>() }
    val ramHistory = remember { mutableStateListOf<Float>() }

    // Initialize histories
    if (cpuHistory.isEmpty()) {
        repeat(15) { cpuHistory.add(20f) }
    }
    if (ramHistory.isEmpty()) {
        repeat(15) { ramHistory.add(40f) }
    }

    // Capture changes and shift arrays
    LaunchedEffect(cpuVal) {
        if (cpuHistory.size > 20) {
            cpuHistory.removeAt(0)
        }
        cpuHistory.add(cpuVal)
    }

    LaunchedEffect(ramVal) {
        if (ramHistory.size > 20) {
            ramHistory.removeAt(0)
        }
        ramHistory.add(ramVal)
    }

    Scaffold(
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = NeonCyan, modifier = Modifier.padding(end = 8.dp))
                Column {
                    Text(
                        text = "系統即時監控",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftWhite
                    )
                    Text(
                        text = "目前監控主機: ${activeHost?.alias ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftGray
                    )
                }
            }

            // Gauges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassmorphicCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cpu_gauge_card"),
                    contentPadding = 12.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CPU 負載", style = MaterialTheme.typography.bodyMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                        CircularGauge(
                            percentage = cpuVal,
                            label = "CPU",
                            primaryColor = NeonPurple,
                            secondaryColor = NeonCyan,
                            modifier = Modifier.height(100.dp)
                        )
                    }
                }

                GlassmorphicCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ram_gauge_card"),
                    contentPadding = 12.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RAM 使用率", style = MaterialTheme.typography.bodyMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                        CircularGauge(
                            percentage = ramVal,
                            label = "RAM",
                            primaryColor = NeonCyan,
                            secondaryColor = NeonGreen,
                            modifier = Modifier.height(100.dp)
                        )
                    }
                }
            }

            // Disk Usage Panel
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("disk_gauge_card"),
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("硬碟儲存空間 (根目錄 /)", style = MaterialTheme.typography.bodyLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
                        Text("剩餘可用及已使用空間比率", style = MaterialTheme.typography.bodySmall, color = SoftGray)
                    }
                    CircularGauge(
                        percentage = diskVal,
                        label = "Disk",
                        primaryColor = NeonPurple,
                        secondaryColor = NeonGreen,
                        modifier = Modifier.height(90.dp)
                    )
                }
            }

            // CPU Trend Chart Card
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("cpu_trend_card"),
                contentPadding = 16.dp
            ) {
                Column {
                    Text("CPU 負載趨勢線 (5秒更新)", style = MaterialTheme.typography.bodyMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    RealtimeLineChart(
                        history = cpuHistory.toList(),
                        lineColor = NeonPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // RAM Trend Chart Card
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("ram_trend_card"),
                contentPadding = 16.dp
            ) {
                Column {
                    Text("記憶體 (RAM) 使用趨勢線 (5秒更新)", style = MaterialTheme.typography.bodyMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    RealtimeLineChart(
                        history = ramHistory.toList(),
                        lineColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // System Constants and Load Averages Panel
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sys_details_card"),
                contentPadding = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("伺服器狀態細節", style = MaterialTheme.typography.bodyMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Text(" 開機時間 (Uptime)", color = SoftGray, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(uptime, color = SoftWhite, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                            Text(" 負載平均值 (Load Average)", color = SoftGray, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(loadAverage, color = SoftWhite, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

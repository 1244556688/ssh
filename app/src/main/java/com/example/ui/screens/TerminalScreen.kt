package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoftWhite
import com.example.ui.viewmodel.SshViewModel

@Composable
fun TerminalScreen(
    viewModel: SshViewModel,
    onBackClick: () -> Unit
) {
    val output by viewModel.connectionManager.terminalOutput.collectAsState()
    val activeHost by viewModel.activeHost.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var fontSize by remember { mutableStateOf(12f) } // adjustable from 8 to 24
    var showFontSizeSlider by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Autoscroll to bottom whenever terminal output receives updates
    LaunchedEffect(output) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold(
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp)
        ) {
            // Screen Header Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Terminal", tint = SoftWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = activeHost?.alias ?: "Active Shell",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftWhite
                        )
                        Text(
                            text = "${activeHost?.username}@${activeHost?.ip}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Font scaling buttons
                    IconButton(onClick = { showFontSizeSlider = !showFontSizeSlider }) {
                        Icon(
                            imageVector = if (fontSize > 14f) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                            contentDescription = "Text Sizing",
                            tint = NeonCyan
                        )
                    }

                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(output))
                        Toast.makeText(context, "Terminal content copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Screen", tint = SoftWhite)
                    }

                    IconButton(onClick = {
                        val clipText = clipboardManager.getText()?.text
                        if (clipText != null) {
                            viewModel.sendTerminalInput(clipText)
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste Clipboard", tint = SoftWhite)
                    }
                }
            }

            // Adjust Font Size Slider Overlay
            if (showFontSizeSlider) {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    cornerRadius = 12.dp,
                    contentPadding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("字型大小: ${fontSize.toInt()} sp", color = SoftWhite, style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 8f..24f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonPurple
                            )
                        )
                    }
                }
            }

            // Terminal Core Output Screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF06070E))
                    .border(1.2.dp, Color(0x1F2CB67D))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = output.ifEmpty { "Connecting to interactive tty shell...\n" },
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSize.sp,
                        color = Color(0xFF5DFF9E), // Vibrant hacker-green terminal text
                        lineHeight = (fontSize + 4).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specialized Modifier Toolbar (Ctrl, Tab, Esc, Arrow Keys, etc.)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SpecialKeyButton(text = "TAB") { viewModel.sendTerminalInput("\t") }
                    SpecialKeyButton(text = "ESC") { viewModel.sendTerminalInput("\u001b") }
                    SpecialKeyButton(text = "Ctrl+C", color = Color(0xFFEF4444)) { viewModel.sendTerminalInput("\u0003") }
                    SpecialKeyButton(text = "Ctrl+Z") { viewModel.sendTerminalInput("\u001a") }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SpecialKeyButton(text = "▲") { viewModel.sendTerminalInput("\u001b[A") }
                    SpecialKeyButton(text = "▼") { viewModel.sendTerminalInput("\u001b[B") }
                    SpecialKeyButton(text = "◀") { viewModel.sendTerminalInput("\u001b[D") }
                    SpecialKeyButton(text = "▶") { viewModel.sendTerminalInput("\u001b[C") }
                }
            }

            // Command Input Box row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("輸入終端指令...", color = SoftGray) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SoftWhite,
                        unfocusedTextColor = SoftWhite,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SoftGray.copy(alpha = 0.4f)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotEmpty()) {
                            viewModel.sendTerminalInput(inputText + "\n")
                            inputText = ""
                        }
                    })
                )

                Button(
                    onClick = {
                        if (inputText.isNotEmpty()) {
                            viewModel.sendTerminalInput(inputText + "\n")
                            inputText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    modifier = Modifier.testTag("terminal_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Send Command")
                }
            }
        }
    }
}

@Composable
fun SpecialKeyButton(
    text: String,
    color: Color = Color(0x3DFFFFFF),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier
            .height(34.dp)
            .testTag("key_button_$text"),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SoftWhite
        )
    }
}

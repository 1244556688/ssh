package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoftWhite

@Composable
fun BiometricLockScreen(
    onUnlockTrigger: () -> Unit,
    onBypassUnlock: () -> Unit
) {
    // Elegant pulsing animation for the lock shield icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Trigger biometric check automatically on entry
    LaunchedEffect(Unit) {
        onUnlockTrigger()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Neon Shield Icon
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Security Shield",
            tint = NeonPurple,
            modifier = Modifier
                .size(110.dp)
                .scale(scaleFactor)
                .padding(bottom = 10.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "MySSH 安全金鑰鎖定",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SoftWhite
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "為確保私鑰及主機連線資訊之安全，請使用生物辨識進行解鎖驗證。",
            style = MaterialTheme.typography.bodyMedium,
            color = SoftGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onUnlockTrigger,
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("biometric_unlock_button")
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "使用指紋 / 臉部解鎖",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Backup unlock bypass button for testing & compatibility assurance
        TextButton(
            onClick = onBypassUnlock,
            modifier = Modifier.testTag("bypass_unlock_button")
        ) {
            Text(
                text = "開發測試安全繞過 (Bypass Key)",
                color = NeonCyan,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

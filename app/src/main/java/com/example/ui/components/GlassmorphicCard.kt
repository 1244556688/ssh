package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.GlassOverlay

/**
 * Custom glassmorphic modifier that draws an elegant frosted sheen,
 * reflection borders, and subtle glowing background gradients.
 */
fun Modifier.glassmorphic(
    cornerRadius: Dp = 20.dp,
    borderColor: Color = DarkSurfaceBorder,
    backgroundColor: Color = DarkSurface
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .drawBehind {
        // Draw elegant high-light gradient behind
        val gradient = Brush.linearGradient(
            colors = listOf(
                GlassOverlay,
                Color.Transparent,
                Color(0x05FFFFFF)
            )
        )
        drawRect(brush = gradient)
    }
    .background(backgroundColor)
    .border(
        BorderStroke(1.2.dp, borderColor),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * A beautiful glassmorphic container card.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = DarkSurfaceBorder,
    backgroundColor: Color = DarkSurface,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .glassmorphic(cornerRadius, borderColor, backgroundColor)
            .padding(contentPadding),
        content = content
    )
}

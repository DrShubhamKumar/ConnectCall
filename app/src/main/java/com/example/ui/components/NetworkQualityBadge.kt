package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.call.NetworkQuality
import com.example.ui.theme.CallGreen

@Composable
fun NetworkQualityBadge(
    quality: NetworkQuality,
    onCycleQuality: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (color, bars) = when (quality) {
        NetworkQuality.GOOD -> CallGreen to 3
        NetworkQuality.FAIR -> Color(0xFFF59E0B) to 2
        NetworkQuality.POOR -> Color(0xFFEF4444) to 1
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x66000000))
            .then(
                if (onCycleQuality != null) Modifier.clickable { onCycleQuality() }
                else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Signal Bars
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (i in 1..3) {
                    val barHeight = (i * 4 + 4).dp
                    val barColor = if (i <= bars) color else Color(0x44FFFFFF)
                    Box(
                        modifier = Modifier
                            .size(width = 3.dp, height = barHeight)
                            .clip(RoundedCornerShape(1.dp))
                            .background(barColor)
                    )
                }
            }

            Text(
                text = quality.label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.CallGreen

@Composable
fun UserAvatar(
    name: String,
    avatarUrl: String? = null,
    size: Dp = 48.dp,
    showOnlineStatus: Boolean = false,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
        .ifEmpty { "U" }

    // Consistent distinct gradient based on name hash
    val colorPair = rememberAvatarGradient(name)

    Box(modifier = modifier.size(size)) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "$name's avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(colorPair)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                )
            }
        }

        if (showOnlineStatus) {
            val statusColor = if (isOnline) CallGreen else Color(0xFF94A3B8)
            val dotSize = (size.value * 0.28f).coerceIn(10f, 20f).dp
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

private fun rememberAvatarGradient(name: String): List<Color> {
    val hash = kotlin.math.abs(name.hashCode())
    val palettes = listOf(
        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)),
        listOf(Color(0xFF0D9488), Color(0xFF059669)),
        listOf(Color(0xFF2563EB), Color(0xFF0284C7)),
        listOf(Color(0xFFD97706), Color(0xFFEA580C)),
        listOf(Color(0xFFDB2777), Color(0xFF9333EA))
    )
    return palettes[hash % palettes.size]
}

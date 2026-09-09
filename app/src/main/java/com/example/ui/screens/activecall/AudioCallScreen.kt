package com.example.ui.screens.activecall

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.call.ActiveCall
import com.example.call.CallStatusState
import com.example.ui.components.AudioCallControls
import com.example.ui.components.NetworkQualityBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallPrimary
import com.example.ui.theme.CallRed
import com.example.ui.theme.Slate900
import java.util.Locale

@Composable
fun AudioCallScreen(
    call: ActiveCall,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    onCycleQuality: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_audio")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse"
    )

    val durationText = run {
        val mins = call.durationSeconds / 60
        val secs = call.durationSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Slate900,
                        Color(0xFF1E1B4B),
                        Slate900
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar: Call Type, Network Quality
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = CallGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Encrypted Audio Call",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            NetworkQualityBadge(
                quality = call.networkQuality,
                onCycleQuality = onCycleQuality
            )
        }

        // Center Content: Avatar & Caller Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pulsing Audio Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                if (call.status == CallStatusState.IN_CALL || call.status == CallStatusState.CONNECTED) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(CallPrimary.copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale * 0.92f)
                            .clip(CircleShape)
                            .background(CallPrimary.copy(alpha = 0.25f))
                    )
                }

                UserAvatar(
                    name = call.participant.name,
                    avatarUrl = call.participant.avatarUrl,
                    size = 130.dp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Caller Name
            Text(
                text = call.participant.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration or State
            if (call.status == CallStatusState.IN_CALL || call.status == CallStatusState.CONNECTED) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = CallGreen,
                    letterSpacing = 1.sp
                )
            } else {
                val statusLabel = call.statusMessage ?: call.status.label
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = when (call.status) {
                        CallStatusState.ENDED, CallStatusState.FAILED, CallStatusState.REJECTED -> CallRed
                        CallStatusState.CALLING, CallStatusState.RINGING -> Color(0xFFFBBF24)
                        else -> Color.White.copy(alpha = 0.8f)
                    }
                )
            }
        }

        // Bottom Controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            AudioCallControls(
                isMuted = call.isMuted,
                isSpeakerOn = call.isSpeakerOn,
                onToggleMute = onToggleMute,
                onToggleSpeaker = onToggleSpeaker,
                onEndCall = onEndCall
            )
        }
    }
}

package com.example.ui.screens.activecall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.call.ActiveCall
import com.example.call.CallStatusState
import com.example.call.CameraLens
import com.example.call.CameraPreview
import com.example.ui.components.NetworkQualityBadge
import com.example.ui.components.UserAvatar
import com.example.ui.components.VideoCallControls
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallPrimary
import com.example.ui.theme.CallRed
import java.util.Locale

@Composable
fun VideoCallScreen(
    call: ActiveCall,
    hasCameraPermission: Boolean,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    onCycleQuality: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationText = run {
        val mins = call.durationSeconds / 60
        val secs = call.durationSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_video")
    val ambientScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // REMOTE PARTICIPANT VIEW (Full Screen)
        if (call.status == CallStatusState.IN_CALL || call.status == CallStatusState.CONNECTED) {
            // Simulated live video stream of remote participant
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(ambientScale),
                contentAlignment = Alignment.Center
            ) {
                if (call.participant.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = call.participant.avatarUrl,
                        contentDescription = "Remote participant video",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF1E1B4B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatar(
                            name = call.participant.name,
                            size = 120.dp
                        )
                    }
                }

                // Vignette gradient overlay for controls readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )
            }
        } else {
            // Connecting / Calling placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0F172A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UserAvatar(
                        name = call.participant.name,
                        avatarUrl = call.participant.avatarUrl,
                        size = 120.dp
                    )

                    Text(
                        text = call.participant.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = call.statusMessage ?: call.status.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = when (call.status) {
                            CallStatusState.ENDED, CallStatusState.FAILED, CallStatusState.REJECTED -> CallRed
                            else -> Color(0xFFFBBF24)
                        }
                    )
                }
            }
        }

        // TOP HUD: Caller info, Duration, Network Quality
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = call.participant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (call.status == CallStatusState.IN_CALL || call.status == CallStatusState.CONNECTED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CallGreen)
                        )
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                } else {
                    Text(
                        text = call.status.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            NetworkQualityBadge(
                quality = call.networkQuality,
                onCycleQuality = onCycleQuality
            )
        }

        // LOCAL CAMERA PREVIEW (Floating Picture-in-Picture)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 64.dp, end = 20.dp)
                .size(width = 110.dp, height = 150.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .background(Color(0xFF1E293B))
                .testTag("local_camera_preview")
        ) {
            CameraPreview(
                isCameraOn = call.isCameraOn,
                cameraLens = call.cameraLens,
                hasCameraPermission = hasCameraPermission,
                modifier = Modifier.fillMaxSize()
            )

            // Lens label badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (call.cameraLens == CameraLens.FRONT) "Front" else "Rear",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // BOTTOM CONTROLS HUD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            VideoCallControls(
                isMuted = call.isMuted,
                isCameraOn = call.isCameraOn,
                onToggleMute = onToggleMute,
                onToggleCamera = onToggleCamera,
                onSwitchCamera = onSwitchCamera,
                onEndCall = onEndCall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

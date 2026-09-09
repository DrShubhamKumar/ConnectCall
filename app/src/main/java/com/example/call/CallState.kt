package com.example.call

import com.example.data.model.CallType
import com.example.data.model.User

enum class CallStatusState(val label: String) {
    IDLE("Idle"),
    CALLING("Calling..."),
    RINGING("Ringing..."),
    CONNECTING("Connecting..."),
    CONNECTED("Connected"),
    IN_CALL("In Call"),
    ENDED("Call Ended"),
    REJECTED("Call Declined"),
    MISSED("Missed Call"),
    BUSY("User Busy"),
    FAILED("Call Failed")
}

enum class NetworkQuality(val label: String) {
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor")
}

enum class CameraLens {
    FRONT, BACK
}

data class ActiveCall(
    val callId: String,
    val participant: User,
    val callType: CallType,
    val isIncoming: Boolean,
    val status: CallStatusState,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isCameraOn: Boolean = true,
    val cameraLens: CameraLens = CameraLens.FRONT,
    val networkQuality: NetworkQuality = NetworkQuality.GOOD,
    val statusMessage: String? = null
)

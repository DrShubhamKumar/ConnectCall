package com.example.call

import android.content.Context
import android.media.AudioManager
import com.example.data.model.CallDirection
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.User
import com.example.data.repository.CallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class CallManager(
    private val context: Context,
    private val repository: CallRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()

    // Transient in-app notification banner for incoming calls (Bonus feature)
    private val _incomingNotification = MutableStateFlow<ActiveCall?>(null)
    val incomingNotification: StateFlow<ActiveCall?> = _incomingNotification.asStateFlow()

    private var callTimerJob: Job? = null
    private var connectionSimJob: Job? = null
    private var timeoutJob: Job? = null

    fun startOutgoingCall(participant: User, callType: CallType) {
        if (participant.isBlocked) {
            _activeCall.value = ActiveCall(
                callId = UUID.randomUUID().toString(),
                participant = participant,
                callType = callType,
                isIncoming = false,
                status = CallStatusState.FAILED,
                statusMessage = "Cannot call blocked contact"
            )
            return
        }

        cancelAllJobs()
        val newCall = ActiveCall(
            callId = UUID.randomUUID().toString(),
            participant = participant,
            callType = callType,
            isIncoming = false,
            status = CallStatusState.CALLING,
            isSpeakerOn = callType == CallType.VIDEO,
            isCameraOn = callType == CallType.VIDEO
        )
        _activeCall.value = newCall
        updateAudioRouting(newCall)

        // Simulate dialing -> ringing -> connected
        connectionSimJob = coroutineScope.launch {
            delay(1500)
            if (_activeCall.value?.status == CallStatusState.CALLING) {
                _activeCall.value = _activeCall.value?.copy(status = CallStatusState.RINGING)
            }
            delay(2000)
            if (_activeCall.value?.status == CallStatusState.RINGING) {
                // If user is offline, there's a chance of Busy or Connect
                if (!participant.isOnline) {
                    _activeCall.value = _activeCall.value?.copy(
                        status = CallStatusState.BUSY,
                        statusMessage = "${participant.name} is unavailable"
                    )
                    logCallHistory(participant, callType, CallDirection.OUTGOING, CallStatus.BUSY, 0)
                    delay(2500)
                    _activeCall.value = null
                    return@launch
                }

                _activeCall.value = _activeCall.value?.copy(status = CallStatusState.CONNECTED)
                delay(600)
                _activeCall.value = _activeCall.value?.copy(status = CallStatusState.IN_CALL)
                startDurationTimer()
            }
        }
    }

    fun simulateIncomingCall(participant: User, callType: CallType) {
        cancelAllJobs()
        val incoming = ActiveCall(
            callId = UUID.randomUUID().toString(),
            participant = participant,
            callType = callType,
            isIncoming = true,
            status = CallStatusState.RINGING,
            isSpeakerOn = callType == CallType.VIDEO,
            isCameraOn = callType == CallType.VIDEO
        )
        _activeCall.value = incoming
        _incomingNotification.value = incoming

        // Ring for up to 25 seconds before marking as missed
        timeoutJob = coroutineScope.launch {
            delay(25000)
            if (_activeCall.value?.status == CallStatusState.RINGING) {
                _activeCall.value = _activeCall.value?.copy(status = CallStatusState.MISSED)
                _incomingNotification.value = null
                logCallHistory(participant, callType, CallDirection.MISSED, CallStatus.MISSED, 0)
                delay(2000)
                _activeCall.value = null
            }
        }
    }

    fun acceptIncomingCall() {
        timeoutJob?.cancel()
        _incomingNotification.value = null
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(status = CallStatusState.CONNECTING)

        coroutineScope.launch {
            delay(1000)
            _activeCall.value = _activeCall.value?.copy(status = CallStatusState.IN_CALL)
            updateAudioRouting(_activeCall.value)
            startDurationTimer()
        }
    }

    fun rejectIncomingCall() {
        timeoutJob?.cancel()
        val current = _activeCall.value ?: return
        _incomingNotification.value = null
        _activeCall.value = current.copy(status = CallStatusState.REJECTED)
        logCallHistory(current.participant, current.callType, CallDirection.INCOMING, CallStatus.REJECTED, 0)

        coroutineScope.launch {
            delay(1500)
            _activeCall.value = null
            restoreAudioRouting()
        }
    }

    fun dismissIncomingNotification() {
        _incomingNotification.value = null
    }

    fun endCall() {
        val current = _activeCall.value ?: return
        cancelAllJobs()
        _incomingNotification.value = null

        val finalDuration = current.durationSeconds
        val finalStatus = if (current.status == CallStatusState.IN_CALL || current.status == CallStatusState.CONNECTED) {
            CallStatus.CONNECTED
        } else if (current.isIncoming) {
            CallStatus.REJECTED
        } else {
            CallStatus.FAILED
        }

        val direction = if (current.isIncoming) CallDirection.INCOMING else CallDirection.OUTGOING

        _activeCall.value = current.copy(status = CallStatusState.ENDED)
        logCallHistory(current.participant, current.callType, direction, finalStatus, finalDuration)

        coroutineScope.launch {
            delay(1500)
            _activeCall.value = null
            restoreAudioRouting()
        }
    }

    fun toggleMute() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isMuted = !it.isMuted)
        }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let {
            val updated = it.copy(isSpeakerOn = !it.isSpeakerOn)
            updateAudioRouting(updated)
            updated
        }
    }

    fun toggleCamera() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isCameraOn = !it.isCameraOn)
        }
    }

    fun switchCamera() {
        _activeCall.value = _activeCall.value?.let {
            val nextLens = if (it.cameraLens == CameraLens.FRONT) CameraLens.BACK else CameraLens.FRONT
            it.copy(cameraLens = nextLens)
        }
    }

    fun cycleNetworkQuality() {
        _activeCall.value = _activeCall.value?.let {
            val next = when (it.networkQuality) {
                NetworkQuality.GOOD -> NetworkQuality.FAIR
                NetworkQuality.FAIR -> NetworkQuality.POOR
                NetworkQuality.POOR -> NetworkQuality.GOOD
            }
            it.copy(networkQuality = next)
        }
    }

    private fun startDurationTimer() {
        callTimerJob?.cancel()
        callTimerJob = coroutineScope.launch {
            var counter = 0
            while (isActive) {
                delay(1000)
                counter++
                _activeCall.value = _activeCall.value?.copy(durationSeconds = counter)

                // Fluctuate network quality occasionally for realism
                if (counter % 18 == 0) {
                    _activeCall.value = _activeCall.value?.copy(networkQuality = NetworkQuality.FAIR)
                } else if (counter % 24 == 0) {
                    _activeCall.value = _activeCall.value?.copy(networkQuality = NetworkQuality.GOOD)
                }
            }
        }
    }

    private fun logCallHistory(
        participant: User,
        callType: CallType,
        direction: CallDirection,
        status: CallStatus,
        duration: Int
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            repository.logCall(
                contactId = participant.id,
                contactName = participant.name,
                contactAvatarUrl = participant.avatarUrl,
                callType = callType,
                direction = direction,
                status = status,
                durationSeconds = duration
            )
        }
    }

    private fun updateAudioRouting(call: ActiveCall?) {
        try {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.isSpeakerphoneOn = call?.isSpeakerOn == true
        } catch (_: Exception) {
            // Ignored if permissions or hardware restricted in environment
        }
    }

    private fun restoreAudioRouting() {
        try {
            audioManager?.mode = AudioManager.MODE_NORMAL
            audioManager?.isSpeakerphoneOn = false
        } catch (_: Exception) {
            // Ignored
        }
    }

    private fun cancelAllJobs() {
        connectionSimJob?.cancel()
        connectionSimJob = null
        timeoutJob?.cancel()
        timeoutJob = null
        callTimerJob?.cancel()
        callTimerJob = null
    }
}

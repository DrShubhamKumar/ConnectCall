package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.call.ActiveCall
import com.example.call.CallManager
import com.example.call.CallStatusState
import com.example.data.local.AppDatabase
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import com.example.data.repository.CallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val callRepository = CallRepository(database.userDao(), database.callDao())
    val authRepository = AuthRepository(database.userDao())
    val callManager = CallManager(application, callRepository)

    // Auth State
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Data streams
    val activeContacts: StateFlow<List<User>> = callRepository.activeContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContacts: StateFlow<List<User>> = callRepository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedContacts: StateFlow<List<User>> = callRepository.blockedContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentContacts: StateFlow<List<User>> = callRepository.recentContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCalls: StateFlow<List<CallRecord>> = callRepository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCalls: StateFlow<List<CallRecord>> = callRepository.recentCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missedCalls: StateFlow<List<CallRecord>> = callRepository.missedCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Call State
    val activeCall: StateFlow<ActiveCall?> = callManager.activeCall
    val incomingNotification: StateFlow<ActiveCall?> = callManager.incomingNotification

    // UI state flags
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            callRepository.initializeSeedDataIfEmpty()
        }
    }

    fun toggleDarkMode(enabled: Boolean? = null) {
        _isDarkMode.value = enabled ?: !_isDarkMode.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Call Actions
    fun startAudioCall(user: User) {
        callManager.startOutgoingCall(user, CallType.AUDIO)
    }

    fun startVideoCall(user: User) {
        callManager.startOutgoingCall(user, CallType.VIDEO)
    }

    fun simulateIncomingCall(user: User, callType: CallType) {
        callManager.simulateIncomingCall(user, callType)
    }

    fun acceptCall() {
        callManager.acceptIncomingCall()
    }

    fun rejectCall() {
        callManager.rejectIncomingCall()
    }

    fun endCall() {
        callManager.endCall()
    }

    fun toggleMute() {
        callManager.toggleMute()
    }

    fun toggleSpeaker() {
        callManager.toggleSpeaker()
    }

    fun toggleCamera() {
        callManager.toggleCamera()
    }

    fun switchCamera() {
        callManager.switchCamera()
    }

    fun cycleNetworkQuality() {
        callManager.cycleNetworkQuality()
    }

    fun dismissIncomingNotification() {
        callManager.dismissIncomingNotification()
    }

    // Contact actions
    fun addContact(name: String, email: String, phone: String) {
        viewModelScope.launch {
            callRepository.addContact(name, email, phone)
        }
    }

    fun setBlocked(userId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            callRepository.setBlocked(userId, isBlocked)
        }
    }

    fun deleteCallRecord(id: Long) {
        viewModelScope.launch {
            callRepository.deleteCall(id)
        }
    }

    fun clearCallHistory() {
        viewModelScope.launch {
            callRepository.clearAllCalls()
        }
    }

    // Auth actions
    fun login(emailOrPhone: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.login(emailOrPhone, pass)
            result.onSuccess { onResult(true, null) }
                .onFailure { onResult(false, it.message) }
        }
    }

    fun register(name: String, email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.register(name, email, pass)
            result.onSuccess { onResult(true, null) }
                .onFailure { onResult(false, it.message) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun updateProfile(name: String, email: String, phone: String, statusMessage: String) {
        viewModelScope.launch {
            authRepository.updateProfile(name, email, phone, statusMessage)
        }
    }

    fun setOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            authRepository.setOnlineStatus(isOnline)
        }
    }
}

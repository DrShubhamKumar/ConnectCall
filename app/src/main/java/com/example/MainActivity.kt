package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.call.ActiveCall
import com.example.call.CallStatusState
import com.example.data.model.CallType
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.components.IncomingCallBanner
import com.example.ui.screens.activecall.AudioCallScreen
import com.example.ui.screens.activecall.IncomingCallScreen
import com.example.ui.screens.activecall.VideoCallScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.calls.CallHistoryScreen
import com.example.ui.screens.contacts.ContactsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.theme.CallPrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                ConnectCallApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ConnectCallApp(viewModel: MainViewModel) {
    val context = LocalContext.current

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val incomingNotification by viewModel.incomingNotification.collectAsStateWithLifecycle()

    val contacts by viewModel.activeContacts.collectAsStateWithLifecycle()
    val blockedContacts by viewModel.blockedContacts.collectAsStateWithLifecycle()
    val recentContacts by viewModel.recentContacts.collectAsStateWithLifecycle()
    val allCalls by viewModel.allCalls.collectAsStateWithLifecycle()
    val recentCalls by viewModel.recentCalls.collectAsStateWithLifecycle()
    val missedCalls by viewModel.missedCalls.collectAsStateWithLifecycle()

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var isSplashDone by rememberSaveable { mutableStateOf(false) }
    var selectedBottomTab by rememberSaveable { mutableIntStateOf(0) } // 0=Home, 1=Contacts, 2=Calls, 3=Profile

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var pendingCallUser by remember { mutableStateOf<User?>(null) }
    var pendingCallType by remember { mutableStateOf<CallType?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = perms[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission

        // If a call was initiated pending permission
        val user = pendingCallUser
        val type = pendingCallType
        if (user != null && type != null) {
            if (type == CallType.VIDEO) {
                viewModel.startVideoCall(user)
            } else {
                viewModel.startAudioCall(user)
            }
            pendingCallUser = null
            pendingCallType = null
        }
    }

    fun requestCallWithPermissions(user: User, type: CallType) {
        val needsAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        val needsCamera = type == CallType.VIDEO && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED

        if (needsAudio || needsCamera) {
            pendingCallUser = user
            pendingCallType = type
            val list = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (type == CallType.VIDEO) list.add(Manifest.permission.CAMERA)
            permissionLauncher.launch(list.toTypedArray())
        } else {
            if (type == CallType.VIDEO) viewModel.startVideoCall(user)
            else viewModel.startAudioCall(user)
        }
    }

    // 1. Splash Screen
    if (!isSplashDone) {
        SplashScreen(
            hasUserSession = currentUser != null,
            onNavigateNext = { isSplashDone = true }
        )
        return
    }

    // 2. Auth Flow (if no user signed in)
    if (currentUser == null) {
        AuthScreen(
            onLogin = { email, pass, cb -> viewModel.login(email, pass, cb) },
            onRegister = { name, email, pass, cb -> viewModel.register(name, email, pass, cb) },
            onAuthSuccess = { /* Automatically navigates due to currentUser update */ }
        )
        return
    }

    // 3. Active Call Fullscreen Overlay
    if (activeCall != null) {
        val currentCall = activeCall!!
        if (currentCall.isIncoming && currentCall.status == CallStatusState.RINGING) {
            IncomingCallScreen(
                call = currentCall,
                onAccept = { viewModel.acceptCall() },
                onDecline = { viewModel.rejectCall() }
            )
            return
        } else if (currentCall.callType == CallType.VIDEO) {
            VideoCallScreen(
                call = currentCall,
                hasCameraPermission = hasCameraPermission,
                onToggleMute = { viewModel.toggleMute() },
                onToggleCamera = { viewModel.toggleCamera() },
                onSwitchCamera = { viewModel.switchCamera() },
                onEndCall = { viewModel.endCall() },
                onCycleQuality = { viewModel.cycleNetworkQuality() }
            )
            return
        } else {
            AudioCallScreen(
                call = currentCall,
                onToggleMute = { viewModel.toggleMute() },
                onToggleSpeaker = { viewModel.toggleSpeaker() },
                onEndCall = { viewModel.endCall() },
                onCycleQuality = { viewModel.cycleNetworkQuality() }
            )
            return
        }
    }

    // 4. Main App Navigation Shell
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedBottomTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedBottomTab == 1) Icons.Default.People else Icons.Outlined.People,
                            contentDescription = "Contacts"
                        )
                    },
                    label = { Text("Contacts") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedBottomTab == 2) Icons.Default.Call else Icons.Outlined.Call,
                            contentDescription = "Calls"
                        )
                    },
                    label = { Text("Calls") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedBottomTab == 3) Icons.Default.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedBottomTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "bottom_tab_transition"
            ) { tab ->
                when (tab) {
                    0 -> HomeScreen(
                        currentUser = currentUser,
                        contacts = contacts,
                        recentContacts = recentContacts,
                        recentCalls = recentCalls,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onStartAudioCall = { requestCallWithPermissions(it, CallType.AUDIO) },
                        onStartVideoCall = { requestCallWithPermissions(it, CallType.VIDEO) },
                        onSimulateIncomingCall = { user, type -> viewModel.simulateIncomingCall(user, type) },
                        onNavigateToContacts = { selectedBottomTab = 1 },
                        onNavigateToCalls = { selectedBottomTab = 2 },
                        onNavigateToProfile = { selectedBottomTab = 3 }
                    )

                    1 -> ContactsScreen(
                        contacts = contacts,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onStartAudioCall = { requestCallWithPermissions(it, CallType.AUDIO) },
                        onStartVideoCall = { requestCallWithPermissions(it, CallType.VIDEO) },
                        onSimulateIncomingCall = { user, type -> viewModel.simulateIncomingCall(user, type) },
                        onAddContact = { name, email, phone -> viewModel.addContact(name, email, phone) },
                        onBlockContact = { userId, blocked -> viewModel.setBlocked(userId, blocked) }
                    )

                    2 -> CallHistoryScreen(
                        allCalls = allCalls,
                        missedCalls = missedCalls,
                        contacts = contacts,
                        onStartAudioCall = { requestCallWithPermissions(it, CallType.AUDIO) },
                        onStartVideoCall = { requestCallWithPermissions(it, CallType.VIDEO) },
                        onDeleteCall = { viewModel.deleteCallRecord(it) },
                        onClearAllCalls = { viewModel.clearCallHistory() }
                    )

                    3 -> ProfileScreen(
                        currentUser = currentUser,
                        blockedContacts = blockedContacts,
                        callHistory = allCalls,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onUpdateProfile = { name, email, phone, status ->
                            viewModel.updateProfile(name, email, phone, status)
                        },
                        onSetOnlineStatus = { viewModel.setOnlineStatus(it) },
                        onUnblockUser = { viewModel.setBlocked(it, false) },
                        onLogout = { viewModel.logout() }
                    )
                }
            }

            // In-app incoming call alert banner (Bonus 1 & 2)
            IncomingCallBanner(
                activeCall = incomingNotification,
                onAccept = { viewModel.acceptCall() },
                onDecline = { viewModel.rejectCall() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

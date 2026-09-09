package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallDirection
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.User
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallPrimary
import com.example.ui.theme.CallRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    currentUser: User?,
    contacts: List<User>,
    recentContacts: List<User>,
    recentCalls: List<CallRecord>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onStartAudioCall: (User) -> Unit,
    onStartVideoCall: (User) -> Unit,
    onSimulateIncomingCall: (User, CallType) -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToCalls: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredContacts = if (searchQuery.isBlank()) {
        contacts
    } else {
        contacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${currentUser?.name ?: "User"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Connect with anyone, anywhere",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier.clickable { onNavigateToProfile() }
                ) {
                    UserAvatar(
                        name = currentUser?.name ?: "You",
                        avatarUrl = currentUser?.avatarUrl,
                        size = 46.dp,
                        showOnlineStatus = true,
                        isOnline = currentUser?.isOnline ?: true
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search people, phone or email...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CallPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("home_search_bar")
            )
        }

        // Simulation Banner (Demonstrating Incoming Call CUJ)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CallPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RingVolume,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Test Incoming Call Flow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Trigger a real incoming call simulation to test the accept/decline screens and call controls:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Video call simulation button
                        val sarah = contacts.firstOrNull { it.name.contains("Sarah", ignoreCase = true) } ?: contacts.firstOrNull()
                        sarah?.let { user ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSimulateIncomingCall(user, CallType.VIDEO) }
                                    .testTag("btn_sim_video_call")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Video (${user.name.take(6)})",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Audio call simulation button
                        val alex = contacts.firstOrNull { it.name.contains("Alex", ignoreCase = true) } ?: contacts.getOrNull(1)
                        alex?.let { user ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CallGreen,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSimulateIncomingCall(user, CallType.AUDIO) }
                                    .testTag("btn_sim_audio_call")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Audio (${user.name.take(5)})",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Contacts (Bonus 5)
        if (recentContacts.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Frequent Contacts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recentContacts, key = { it.id }) { contact ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(72.dp)
                                    .clickable { onStartAudioCall(contact) }
                            ) {
                                UserAvatar(
                                    name = contact.name,
                                    avatarUrl = contact.avatarUrl,
                                    size = 56.dp,
                                    showOnlineStatus = true,
                                    isOnline = contact.isOnline
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = contact.name.split(" ").firstOrNull() ?: contact.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Calls Section
        if (recentCalls.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Calls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToCalls) {
                        Text("See All", color = CallPrimary)
                    }
                }
            }

            items(recentCalls.take(3), key = { it.id }) { call ->
                RecentCallItem(
                    call = call,
                    onCallback = {
                        val contact = contacts.firstOrNull { it.id == call.contactId } ?: User(
                            id = call.contactId,
                            name = call.contactName,
                            email = "",
                            phone = "",
                            avatarUrl = call.contactAvatarUrl
                        )
                        if (call.callType == CallType.VIDEO) onStartVideoCall(contact)
                        else onStartAudioCall(contact)
                    }
                )
            }
        }

        // Contacts list header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Users & Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToContacts) {
                    Text("View Contacts", color = CallPrimary)
                }
            }
        }

        // Contacts List
        items(filteredContacts, key = { it.id }) { contact ->
            ContactItemCard(
                user = contact,
                onAudioCall = { onStartAudioCall(contact) },
                onVideoCall = { onStartVideoCall(contact) }
            )
        }
    }
}

@Composable
fun ContactItemCard(
    user: User,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UserAvatar(
                name = user.name,
                avatarUrl = user.avatarUrl,
                size = 48.dp,
                showOnlineStatus = true,
                isOnline = user.isOnline
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (user.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (user.isOnline) CallGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Audio Call Button
            IconButton(
                onClick = onAudioCall,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CallGreen.copy(alpha = 0.12f))
                    .testTag("btn_audio_${user.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Audio Call ${user.name}",
                    tint = CallGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Video Call Button
            IconButton(
                onClick = onVideoCall,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CallPrimary.copy(alpha = 0.12f))
                    .testTag("btn_video_${user.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video Call ${user.name}",
                    tint = CallPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RecentCallItem(
    call: CallRecord,
    onCallback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserAvatar(
                name = call.contactName,
                avatarUrl = call.contactAvatarUrl,
                size = 42.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.contactName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val (dirIcon, dirColor) = when (call.direction) {
                        CallDirection.INCOMING -> Icons.Default.CallReceived to CallGreen
                        CallDirection.OUTGOING -> Icons.Default.CallMade to CallPrimary
                        CallDirection.MISSED -> Icons.Default.PhoneMissed to CallRed
                    }
                    Icon(
                        imageVector = dirIcon,
                        contentDescription = null,
                        tint = dirColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${if (call.callType == CallType.VIDEO) "Video" else "Audio"} • ${formatCallTime(call.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val durationText = if (call.direction == CallDirection.MISSED) {
                "Missed"
            } else {
                val mins = call.durationSeconds / 60
                val secs = call.durationSeconds % 60
                String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
            }

            Text(
                text = durationText,
                style = MaterialTheme.typography.bodySmall,
                color = if (call.direction == CallDirection.MISSED) CallRed else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = onCallback,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (call.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Call back",
                    tint = CallPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun formatCallTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "Today, ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))}"
        diff < 48 * 60 * 60 * 1000 -> "Yesterday, ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))}"
        else -> SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}

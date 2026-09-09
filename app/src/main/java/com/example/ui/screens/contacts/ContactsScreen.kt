package com.example.ui.screens.contacts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallType
import com.example.data.model.User
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallPrimary
import com.example.ui.theme.CallRed

@Composable
fun ContactsScreen(
    contacts: List<User>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onStartAudioCall: (User) -> Unit,
    onStartVideoCall: (User) -> Unit,
    onSimulateIncomingCall: (User, CallType) -> Unit,
    onAddContact: (String, String, String) -> Unit,
    onBlockContact: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var filterOnlineOnly by remember { mutableStateOf(false) }

    val filteredList = contacts.filter { contact ->
        val matchesQuery = searchQuery.isBlank() ||
            contact.name.contains(searchQuery, ignoreCase = true) ||
            contact.phone.contains(searchQuery, ignoreCase = true) ||
            contact.email.contains(searchQuery, ignoreCase = true)
        val matchesOnline = !filterOnlineOnly || contact.isOnline
        matchesQuery && matchesOnline
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Contacts",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${contacts.size} connections available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .testTag("btn_add_contact_icon")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Contact",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search people...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
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
                    .padding(horizontal = 20.dp)
                    .testTag("contacts_search_input")
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !filterOnlineOnly,
                    onClick = { filterOnlineOnly = false },
                    label = { Text("All (${contacts.size})") }
                )
                FilterChip(
                    selected = filterOnlineOnly,
                    onClick = { filterOnlineOnly = true },
                    label = { Text("Online (${contacts.count { it.isOnline }})") }
                )
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No contacts found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try a different search query or add a new contact.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 84.dp)
                ) {
                    items(filteredList, key = { it.id }) { user ->
                        ContactRow(
                            user = user,
                            onAudioCall = { onStartAudioCall(user) },
                            onVideoCall = { onStartVideoCall(user) },
                            onSimulateIncoming = { type -> onSimulateIncomingCall(user, type) },
                            onBlock = { onBlockContact(user.id, true) }
                        )
                    }
                }
            }
        }

        // Add Contact Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = CallPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 84.dp)
                .testTag("fab_add_contact")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email, phone ->
                onAddContact(name, email, phone)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ContactRow(
    user: User,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    onSimulateIncoming: (CallType) -> Unit,
    onBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UserAvatar(
                name = user.name,
                avatarUrl = user.avatarUrl,
                size = 50.dp,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (user.isOnline) CallGreen else Color(0xFF94A3B8))
                    )
                    Text(
                        text = if (user.isOnline) "Online" else "Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (user.isOnline) CallGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ${user.phone.ifBlank { user.email }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Audio Call Button
            IconButton(
                onClick = onAudioCall,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CallGreen.copy(alpha = 0.12f))
                    .testTag("contact_audio_${user.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Audio call ${user.name}",
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
                    .testTag("contact_video_${user.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video call ${user.name}",
                    tint = CallPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // More Options Dropdown
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Simulate Incoming Video Call") },
                        leadingIcon = {
                            Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = CallPrimary)
                        },
                        onClick = {
                            menuExpanded = false
                            onSimulateIncoming(CallType.VIDEO)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Simulate Incoming Audio Call") },
                        leadingIcon = {
                            Icon(Icons.Default.Call, contentDescription = null, tint = CallGreen)
                        },
                        onClick = {
                            menuExpanded = false
                            onSimulateIncoming(CallType.AUDIO)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Block User", color = CallRed) },
                        leadingIcon = {
                            Icon(Icons.Default.Block, contentDescription = null, tint = CallRed)
                        },
                        onClick = {
                            menuExpanded = false
                            onBlock()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = CallRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Please enter contact name"
                        return@Button
                    }
                    onConfirm(name, email, phone)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

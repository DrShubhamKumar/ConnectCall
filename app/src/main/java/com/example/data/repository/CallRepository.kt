package com.example.data.repository

import com.example.data.local.CallDao
import com.example.data.local.UserDao
import com.example.data.model.CallDirection
import com.example.data.model.CallRecord
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CallRepository(
    private val userDao: UserDao,
    private val callDao: CallDao
) {
    val activeContacts: Flow<List<User>> = userDao.getActiveContacts()
    val allContacts: Flow<List<User>> = userDao.getAllContacts()
    val blockedContacts: Flow<List<User>> = userDao.getBlockedContacts()
    val recentContacts: Flow<List<User>> = userDao.getRecentContacts()
    val allCalls: Flow<List<CallRecord>> = callDao.getAllCalls()
    val recentCalls: Flow<List<CallRecord>> = callDao.getRecentCalls(5)
    val missedCalls: Flow<List<CallRecord>> = callDao.getMissedCalls()

    fun searchContacts(query: String): Flow<List<User>> = userDao.searchContacts(query)

    suspend fun initializeSeedDataIfEmpty() {
        if (userDao.getUserCount() == 0) {
            val seedContacts = listOf(
                User(
                    id = "user_sarah",
                    name = "Sarah Johnson",
                    email = "sarah.johnson@example.com",
                    phone = "+1 (555) 234-5678",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                    isOnline = true,
                    statusMessage = "Available for audio/video chat",
                    callCount = 12
                ),
                User(
                    id = "user_john",
                    name = "John Smith",
                    email = "john.smith@example.com",
                    phone = "+1 (555) 345-6789",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                    isOnline = false,
                    statusMessage = "In a meeting, call later",
                    callCount = 5
                ),
                User(
                    id = "user_alex",
                    name = "Alex Wilson",
                    email = "alex.wilson@example.com",
                    phone = "+1 (555) 456-7890",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                    isOnline = true,
                    statusMessage = "Online and ready to connect",
                    callCount = 8
                ),
                User(
                    id = "user_emily",
                    name = "Emily Davis",
                    email = "emily.davis@example.com",
                    phone = "+1 (555) 567-8901",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                    isOnline = true,
                    statusMessage = "Working from home",
                    callCount = 3
                ),
                User(
                    id = "user_michael",
                    name = "Michael Brown",
                    email = "michael.brown@example.com",
                    phone = "+1 (555) 678-9012",
                    avatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200",
                    isOnline = false,
                    statusMessage = "Stepped away",
                    callCount = 2
                ),
                User(
                    id = "user_sophia",
                    name = "Sophia Taylor",
                    email = "sophia.taylor@example.com",
                    phone = "+1 (555) 789-0123",
                    avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200",
                    isOnline = true,
                    statusMessage = "Traveling this week",
                    callCount = 4
                )
            )
            userDao.insertUsers(seedContacts)

            val now = System.currentTimeMillis()
            val sampleCalls = listOf(
                CallRecord(
                    contactId = "user_sarah",
                    contactName = "Sarah Johnson",
                    contactAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                    callType = CallType.VIDEO,
                    direction = CallDirection.INCOMING,
                    status = CallStatus.CONNECTED,
                    durationSeconds = 155, // 02:35
                    timestamp = now - (15 * 60 * 1000) // 15 mins ago
                ),
                CallRecord(
                    contactId = "user_john",
                    contactName = "John Smith",
                    contactAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                    callType = CallType.AUDIO,
                    direction = CallDirection.MISSED,
                    status = CallStatus.MISSED,
                    durationSeconds = 0,
                    timestamp = now - (4 * 3600 * 1000) // 4 hours ago
                ),
                CallRecord(
                    contactId = "user_alex",
                    contactName = "Alex Wilson",
                    contactAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                    callType = CallType.AUDIO,
                    direction = CallDirection.OUTGOING,
                    status = CallStatus.CONNECTED,
                    durationSeconds = 252, // 04:12
                    timestamp = now - (26 * 3600 * 1000) // Yesterday
                ),
                CallRecord(
                    contactId = "user_emily",
                    contactName = "Emily Davis",
                    contactAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                    callType = CallType.VIDEO,
                    direction = CallDirection.OUTGOING,
                    status = CallStatus.CONNECTED,
                    durationSeconds = 480, // 08:00
                    timestamp = now - (48 * 3600 * 1000)
                )
            )
            for (call in sampleCalls) {
                callDao.insertCall(call)
            }
        }
    }

    suspend fun addContact(name: String, email: String, phone: String) {
        val newContact = User(
            id = "user_" + UUID.randomUUID().toString().take(8),
            name = name,
            email = email,
            phone = phone,
            isOnline = true,
            statusMessage = "Hey there! I am using ConnectCall."
        )
        userDao.insertUser(newContact)
    }

    suspend fun setBlocked(id: String, blocked: Boolean) {
        userDao.setBlocked(id, blocked)
    }

    suspend fun logCall(
        contactId: String,
        contactName: String,
        contactAvatarUrl: String,
        callType: CallType,
        direction: CallDirection,
        status: CallStatus,
        durationSeconds: Int
    ) {
        val record = CallRecord(
            contactId = contactId,
            contactName = contactName,
            contactAvatarUrl = contactAvatarUrl,
            callType = callType,
            direction = direction,
            status = status,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis()
        )
        callDao.insertCall(record)
        userDao.incrementCallCount(contactId)
    }

    suspend fun deleteCall(id: Long) = callDao.deleteCall(id)

    suspend fun clearAllCalls() = callDao.clearAllCalls()
}

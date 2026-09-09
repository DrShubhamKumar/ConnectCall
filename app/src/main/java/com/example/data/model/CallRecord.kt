package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CallType {
    AUDIO, VIDEO
}

enum class CallDirection {
    INCOMING, OUTGOING, MISSED
}

enum class CallStatus {
    CONNECTED, MISSED, REJECTED, BUSY, FAILED
}

@Entity(tableName = "call_records")
data class CallRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: String,
    val contactName: String,
    val contactAvatarUrl: String = "",
    val callType: CallType,
    val direction: CallDirection,
    val status: CallStatus,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

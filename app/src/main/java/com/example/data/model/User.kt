package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String = "",
    val isOnline: Boolean = true,
    val statusMessage: String = "Available to connect",
    val isBlocked: Boolean = false,
    val isCurrentUser: Boolean = false,
    val callCount: Int = 0
)

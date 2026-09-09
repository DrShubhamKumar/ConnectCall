package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val userDao: UserDao) {
    val currentUser: Flow<User?> = userDao.getCurrentUser()

    suspend fun hasSession(): Boolean {
        return userDao.getCurrentUserSync() != null
    }

    suspend fun login(emailOrPhone: String, password: String): Result<User> {
        val trimmed = emailOrPhone.trim()
        if (trimmed.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email/Phone and Password cannot be empty"))
        }

        // Check if there is already a current user
        val existing = userDao.getCurrentUserSync()
        val user = if (existing != null) {
            existing.copy(email = trimmed, isOnline = true)
        } else {
            User(
                id = "me_user",
                name = if (trimmed.contains("@")) trimmed.substringBefore("@").replaceFirstChar { it.uppercase() } else "Demo User",
                email = trimmed,
                phone = if (trimmed.contains("@")) "+1 (555) 019-2831" else trimmed,
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                isOnline = true,
                statusMessage = "Available on ConnectCall",
                isCurrentUser = true
            )
        }
        userDao.insertUser(user)
        return Result.success(user)
    }

    suspend fun register(name: String, email: String, password: String): Result<User> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields are required"))
        }
        val user = User(
            id = "me_user",
            name = name.trim(),
            email = email.trim(),
            phone = "+1 (555) 019-2831",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            isOnline = true,
            statusMessage = "Hey! I just joined ConnectCall",
            isCurrentUser = true
        )
        userDao.insertUser(user)
        return Result.success(user)
    }

    suspend fun updateProfile(name: String, email: String, phone: String, statusMessage: String) {
        val current = userDao.getCurrentUserSync() ?: return
        val updated = current.copy(
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            statusMessage = statusMessage.trim()
        )
        userDao.updateUser(updated)
    }

    suspend fun setOnlineStatus(isOnline: Boolean) {
        val current = userDao.getCurrentUserSync() ?: return
        userDao.setOnlineStatus(current.id, isOnline)
    }

    suspend fun logout() {
        val current = userDao.getCurrentUserSync() ?: return
        // Mark as non current or remove current user
        userDao.updateUser(current.copy(isCurrentUser = false, isOnline = false))
    }
}

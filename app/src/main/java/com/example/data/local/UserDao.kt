package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isCurrentUser = 0 AND isBlocked = 0 ORDER BY name ASC")
    fun getActiveContacts(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE isCurrentUser = 0 ORDER BY name ASC")
    fun getAllContacts(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE isCurrentUser = 0 AND isBlocked = 1 ORDER BY name ASC")
    fun getBlockedContacts(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE isCurrentUser = 0 AND isBlocked = 0 ORDER BY callCount DESC, name ASC LIMIT 6")
    fun getRecentContacts(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getContactById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserSync(): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET isBlocked = :isBlocked WHERE id = :id")
    suspend fun setBlocked(id: String, isBlocked: Boolean)

    @Query("UPDATE users SET callCount = callCount + 1 WHERE id = :id")
    suspend fun incrementCallCount(id: String)

    @Query("UPDATE users SET isOnline = :isOnline WHERE id = :id")
    suspend fun setOnlineStatus(id: String, isOnline: Boolean)

    @Query("SELECT * FROM users WHERE isCurrentUser = 0 AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%')")
    fun searchContacts(query: String): Flow<List<User>>
}

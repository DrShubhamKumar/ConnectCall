package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CallDirection
import com.example.data.model.CallRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCalls(limit: Int = 5): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records WHERE direction = 'MISSED' OR status = 'MISSED' ORDER BY timestamp DESC")
    fun getMissedCalls(): Flow<List<CallRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(record: CallRecord): Long

    @Query("DELETE FROM call_records WHERE id = :id")
    suspend fun deleteCall(id: Long)

    @Query("DELETE FROM call_records")
    suspend fun clearAllCalls()
}

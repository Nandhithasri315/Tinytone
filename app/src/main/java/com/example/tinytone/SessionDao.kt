package com.example.tinytone

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun getTotalSessions(): Int

    @Query("SELECT AVG(accuracyScore) FROM sessions WHERE difficulty = :diff")
    suspend fun getAverageAccuracyForDifficulty(diff: String): Float?

    @Query("SELECT SUM(durationMs) FROM sessions")
    suspend fun getTotalTimeSpent(): Long?

    @Query("SELECT * FROM sessions WHERE timestamp >= :sinceTimestamp")
    suspend fun getSessionsSince(sinceTimestamp: Long): List<SessionEntity>
}

package com.example.tinytone

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BadgeDao {

    @Query("SELECT * FROM badges")
    suspend fun getAllBadges(): List<BadgeEntity>

    @Query("SELECT * FROM badges WHERE earned = 1")
    suspend fun getEarnedBadges(): List<BadgeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(badges: List<BadgeEntity>)

    @Query("UPDATE badges SET earned = 1 WHERE id = :badgeId")
    suspend fun earnBadge(badgeId: String)

    @Query("SELECT COUNT(*) FROM badges WHERE earned = 1")
    suspend fun getEarnedCount(): Int
}
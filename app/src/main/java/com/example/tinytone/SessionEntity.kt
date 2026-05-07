package com.example.tinytone

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val wordId: Int,
    val accuracyScore: Int,
    val durationMs: Long,
    val timestamp: Long,
    val difficulty: String
)

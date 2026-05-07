package com.example.tinytone

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    val category: String = "Basic",
    val difficulty: String = "EASY",
    val lastScore: Int = 0,
    val attemptCount: Int = 0
)
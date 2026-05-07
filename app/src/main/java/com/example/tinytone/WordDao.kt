package com.example.tinytone

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WordDao {

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): WordEntity?

    @Query("SELECT * FROM words WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWordByCategory(category: String): WordEntity?

    @Insert
    suspend fun insertAll(words: List<WordEntity>)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("SELECT * FROM words WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWordByDifficulty(difficulty: String): WordEntity?

    @Query("SELECT * FROM words WHERE attemptCount > 0 AND lastScore < 75 ORDER BY RANDOM() LIMIT 1")
    suspend fun getWeakWord(): WordEntity?

    @Query("SELECT * FROM words WHERE category = :category AND (:difficulty = '' OR difficulty = :difficulty) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWordByCategoryAndDifficulty(category: String, difficulty: String): WordEntity?

    @Query("UPDATE words SET lastScore = :score, attemptCount = attemptCount + 1 WHERE id = :id")
    suspend fun updateWordScore(id: Int, score: Int)
}
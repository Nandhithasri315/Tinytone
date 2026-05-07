package com.example.tinytone

class AppRepository(
    private val wordDao: WordDao,
    private val badgeDao: BadgeDao,
    private val sessionDao: SessionDao
) {
    suspend fun getWordCount() = wordDao.getWordCount()
    suspend fun insertWords(words: List<WordEntity>) = wordDao.insertAll(words)
    suspend fun getRandomWord() = wordDao.getRandomWord()
    suspend fun getRandomWordByDifficulty(diff: String) = wordDao.getRandomWordByDifficulty(diff)
    suspend fun getRandomWordByCategory(category: String, difficulty: String = "") =
        wordDao.getRandomWordByCategoryAndDifficulty(category, difficulty)
    suspend fun getWeakWord() = wordDao.getWeakWord()
    suspend fun updateWordScore(id: Int, score: Int) = wordDao.updateWordScore(id, score)

    suspend fun insertSession(session: SessionEntity) = sessionDao.insertSession(session)
    suspend fun getTotalSessions() = sessionDao.getTotalSessions()
    suspend fun getAverageAccuracyForDifficulty(diff: String) = sessionDao.getAverageAccuracyForDifficulty(diff)
    suspend fun getTotalTimeSpent() = sessionDao.getTotalTimeSpent()
    suspend fun getSessionsSince(sinceTimestamp: Long) = sessionDao.getSessionsSince(sinceTimestamp)

    suspend fun getAllBadges() = badgeDao.getAllBadges()
}

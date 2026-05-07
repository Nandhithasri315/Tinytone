package com.example.tinytone

object BadgeManager {

    /**
     * Checks and awards badges based on current progress.
     * Synchronized with TinyToneApp badge IDs.
     */
    suspend fun checkAndAward(
        badgeDao: BadgeDao,
        totalStars: Int,
        consecutiveStars: Int,
        accuracyPercent: Int,
        totalWordsPracticed: Int,
        sessionStars: Int
    ) {
        // 1. First Star
        if (totalStars >= 1) badgeDao.earnBadge("first_star")

        // 2. Hot Streak (3 correct in a row)
        if (consecutiveStars >= 3) badgeDao.earnBadge("streak_3")

        // 3. Word Explorer (10 words)
        if (totalWordsPracticed >= 10) badgeDao.earnBadge("words_10")

        // 4. Word Master (50 words)
        if (totalWordsPracticed >= 50) badgeDao.earnBadge("words_50")

        // 5. Perfect Score (100% accuracy)
        if (accuracyPercent >= 100) badgeDao.earnBadge("perfect_score")

        // Note: "voice_pro" is handled inside voice practice activities.
    }
}

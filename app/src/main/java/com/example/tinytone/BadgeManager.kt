package com.example.tinytone

object BadgeManager {

    // Call this after every attempt
    suspend fun checkAndAward(
        badgeDao: BadgeDao,
        totalStars: Int,
        consecutiveStars: Int,
        accuracyPercent: Int,
        totalWordsPracticed: Int,
        sessionStars: Int
    ) {
        // Seed badges if empty
        if (badgeDao.getAllBadges().isEmpty()) {
            seedBadges(badgeDao)
        }

        // Check each badge condition
        if (totalStars >= 1)           badgeDao.earnBadge("first_star")
        if (consecutiveStars >= 3)     badgeDao.earnBadge("on_fire")
        if (totalStars >= 10)          badgeDao.earnBadge("champion")
        if (accuracyPercent >= 90)     badgeDao.earnBadge("sharp_shooter")
        if (totalWordsPracticed >= 20) badgeDao.earnBadge("word_master")
        if (sessionStars >= 5)         badgeDao.earnBadge("perfect_day")
    }

    private suspend fun seedBadges(badgeDao: BadgeDao) {
        val badges = listOf(
            BadgeEntity("first_star",    "First Star",      "🌟", "Earn your first star!"),
            BadgeEntity("on_fire",       "On Fire",         "🔥", "Get 3 stars in a row!"),
            BadgeEntity("champion",      "Champion",        "🏆", "Earn 10 total stars!"),
            BadgeEntity("sharp_shooter", "Sharp Shooter",   "🎯", "Get 90%+ accuracy!"),
            BadgeEntity("word_master",   "Word Master",     "📚", "Practice 20 words!"),
            BadgeEntity("perfect_day",   "Perfect Day",     "🌈", "Get 5 stars in one session!")
        )
        badgeDao.insertAll(badges)
    }
}
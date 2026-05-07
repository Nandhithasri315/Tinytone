package com.example.tinytone

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TinyToneApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Seed the database on app start if it is empty
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@TinyToneApp)
            val wordCount = db.wordDao().getWordCount()
            if (wordCount == 0) {
                db.wordDao().insertAll(WORD_LIST)
            }
            val badgeCount = db.badgeDao().getAllBadges().size
            if (badgeCount == 0) {
                db.badgeDao().insertAll(BADGE_LIST)
            }
        }
    }

    companion object {

        val WORD_LIST = listOf(
            // ─── EASY – Animals ───────────────────────────────────────────────
            WordEntity(word = "cat",      category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "dog",      category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "cow",      category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "hen",      category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "pig",      category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "fish",     category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "frog",     category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "duck",     category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "bear",     category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "lion",     category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "bird",     category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "goat",     category = "Animals",   difficulty = "EASY"),

            // ─── EASY – Foods (Unified to "Foods") ─────────────────────────────
            WordEntity(word = "apple",    category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "milk",     category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "egg",      category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "bread",    category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "cake",     category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "rice",     category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "juice",    category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "soup",     category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "mango",    category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "corn",     category = "Foods",      difficulty = "EASY"),

            // ─── EASY – Colors ────────────────────────────────────────────────
            WordEntity(word = "red",      category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "blue",     category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "green",    category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "pink",     category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "black",    category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "white",    category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "gold",     category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "brown",    category = "Colors",    difficulty = "EASY"),

            // ─── MEDIUM – Foods ───────────────────────────────────────────────
            WordEntity(word = "banana",   category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "orange",   category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "potato",   category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "tomato",   category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "carrot",   category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "butter",   category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "noodles",  category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "sandwich", category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "biscuit",  category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "yogurt",   category = "Foods",      difficulty = "MEDIUM"),

            // ─── School (Misc) ────────────────────────────────────────────────
            WordEntity(word = "pencil",   category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "teacher",  category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "student",  category = "School",    difficulty = "MEDIUM")
        )

        val BADGE_LIST = listOf(
            BadgeEntity(id = "first_star",    title = "First Star",    emoji = "⭐", description = "Earn your first star",      earned = false),
            BadgeEntity(id = "streak_3",      title = "Hot Streak",    emoji = "🔥", description = "3 correct in a row",        earned = false),
            BadgeEntity(id = "words_10",      title = "Word Explorer", emoji = "📖", description = "Practice 10 words",         earned = false),
            BadgeEntity(id = "words_50",      title = "Word Master",   emoji = "🏆", description = "Practice 50 words",         earned = false),
            BadgeEntity(id = "perfect_score", title = "Perfect!",      emoji = "💎", description = "Get 100% accuracy",        earned = false),
            BadgeEntity(id = "voice_pro",     title = "Voice Pro",     emoji = "🎤", description = "Complete a voice exercise", earned = false)
        )
    }
}

package com.example.tinytone

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TinyToneApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@TinyToneApp)
            val wordCount = db.wordDao().getWordCount()
            // In a real app, we might handle updates better, but for now we seed if empty
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
            // ─── ANIMALS ──────────────────────────────────────────────────────
            WordEntity(word = "Cat",      category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "Dog",      category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "Lion",     category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "Pig",      category = "Animals",   difficulty = "EASY"),
            WordEntity(word = "Monkey",   category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "Turtle",   category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "Rabbit",   category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "Giraffe",  category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "Crocodile",category = "Animals",   difficulty = "HARD"),
            WordEntity(word = "Elephant", category = "Animals",   difficulty = "HARD"),
            WordEntity(word = "Rhinoceros",category = "Animals",   difficulty = "HARD"),
            WordEntity(word = "Chameleon", category = "Animals",   difficulty = "HARD"),

            // ─── FOODS ────────────────────────────────────────────────────────
            WordEntity(word = "Apple",    category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "Milk",     category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "Bread",    category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "Egg",      category = "Foods",      difficulty = "EASY"),
            WordEntity(word = "Banana",   category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "Pizza",    category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "Carrot",   category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "Cookie",   category = "Foods",      difficulty = "MEDIUM"),
            WordEntity(word = "Spaghetti",category = "Foods",      difficulty = "HARD"),
            WordEntity(word = "Pomegranate",category = "Foods",    difficulty = "HARD"),
            WordEntity(word = "Cauliflower",category = "Foods",    difficulty = "HARD"),
            WordEntity(word = "Asparagus", category = "Foods",      difficulty = "HARD"),

            // ─── COLORS ───────────────────────────────────────────────────────
            WordEntity(word = "Red",      category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "Blue",     category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "Pink",     category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "Gold",     category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "Yellow",   category = "Colors",    difficulty = "MEDIUM"),
            WordEntity(word = "Purple",   category = "Colors",    difficulty = "MEDIUM"),
            WordEntity(word = "Orange",   category = "Colors",    difficulty = "MEDIUM"),
            WordEntity(word = "Silver",   category = "Colors",    difficulty = "MEDIUM"),
            WordEntity(word = "Turquoise",category = "Colors",    difficulty = "HARD"),
            WordEntity(word = "Lavender", category = "Colors",    difficulty = "HARD"),
            WordEntity(word = "Magenta",  category = "Colors",    difficulty = "HARD"),
            WordEntity(word = "Crimson",  category = "Colors",    difficulty = "HARD")
        )

        val BADGE_LIST = listOf(
            BadgeEntity(id = "first_star",    title = "First Star",    emoji = "⭐", description = "Earn your first star"),
            BadgeEntity(id = "streak_3",      title = "Hot Streak",    emoji = "🔥", description = "3 correct in a row"),
            BadgeEntity(id = "words_10",      title = "Word Explorer", emoji = "📖", description = "Practice 10 words"),
            BadgeEntity(id = "words_50",      title = "Word Master",   emoji = "🏆", description = "Practice 50 words"),
            BadgeEntity(id = "perfect_score", title = "Perfect!",      emoji = "💎", description = "Get 100% accuracy"),
            BadgeEntity(id = "voice_pro",     title = "Voice Pro",     emoji = "🎤", description = "Complete a voice exercise")
        )
    }
}

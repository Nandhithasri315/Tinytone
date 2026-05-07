package com.example.tinytone

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TinyToneApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Seed the database on every app start if it is empty
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

            // ─── EASY – Food ──────────────────────────────────────────────────
            WordEntity(word = "apple",    category = "Food",      difficulty = "EASY"),
            WordEntity(word = "milk",     category = "Food",      difficulty = "EASY"),
            WordEntity(word = "egg",      category = "Food",      difficulty = "EASY"),
            WordEntity(word = "bread",    category = "Food",      difficulty = "EASY"),
            WordEntity(word = "cake",     category = "Food",      difficulty = "EASY"),
            WordEntity(word = "rice",     category = "Food",      difficulty = "EASY"),
            WordEntity(word = "juice",    category = "Food",      difficulty = "EASY"),
            WordEntity(word = "soup",     category = "Food",      difficulty = "EASY"),
            WordEntity(word = "mango",    category = "Food",      difficulty = "EASY"),
            WordEntity(word = "corn",     category = "Food",      difficulty = "EASY"),

            // ─── EASY – Colors ────────────────────────────────────────────────
            WordEntity(word = "red",      category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "blue",     category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "green",    category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "pink",     category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "black",    category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "white",    category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "gold",     category = "Colors",    difficulty = "EASY"),
            WordEntity(word = "brown",    category = "Colors",    difficulty = "EASY"),

            // ─── EASY – Body ──────────────────────────────────────────────────
            WordEntity(word = "eye",      category = "Body",      difficulty = "EASY"),
            WordEntity(word = "ear",      category = "Body",      difficulty = "EASY"),
            WordEntity(word = "nose",     category = "Body",      difficulty = "EASY"),
            WordEntity(word = "hand",     category = "Body",      difficulty = "EASY"),
            WordEntity(word = "foot",     category = "Body",      difficulty = "EASY"),
            WordEntity(word = "lip",      category = "Body",      difficulty = "EASY"),
            WordEntity(word = "knee",     category = "Body",      difficulty = "EASY"),
            WordEntity(word = "back",     category = "Body",      difficulty = "EASY"),
            WordEntity(word = "arm",      category = "Body",      difficulty = "EASY"),
            WordEntity(word = "leg",      category = "Body",      difficulty = "EASY"),

            // ─── MEDIUM – Animals ──────────────────────────────────────────────
            WordEntity(word = "rabbit",   category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "parrot",   category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "turtle",   category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "monkey",   category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "elephant", category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "dolphin",  category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "giraffe",  category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "penguin",  category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "leopard",  category = "Animals",   difficulty = "MEDIUM"),
            WordEntity(word = "camel",    category = "Animals",   difficulty = "MEDIUM"),

            // ─── MEDIUM – Food ────────────────────────────────────────────────
            WordEntity(word = "banana",   category = "Food",      difficulty = "MEDIUM"),
            WordEntity(word = "orange",   category = "Food",      difficulty = "MEDIUM"),
            WordEntity(word = "potato",   category = "Food",      difficulty = "MEDIUM"),
            WordEntity(word = "tomato",   category = "Food",      difficulty = "MEDIUM"),
            WordEntity(word = "carrot",   category = "Food",      difficulty = "MEDIUM"),
            WordEntity(word = "butter",   category = "Food",      difficulty = "MEDIUM"),
            WordEntity(word = "noodles",  category = "Food",      difficulty = "MEDIUM"),
            WordEntity(word = "sandwich", category = "Food",      difficulty = "MEDIUM"),
            WordEntity(word = "biscuit",  category = "Food",      difficulty = "MEDIUM"),
            WordEntity(word = "yogurt",   category = "Food",      difficulty = "MEDIUM"),

            // ─── MEDIUM – Nature ──────────────────────────────────────────────
            WordEntity(word = "flower",   category = "Nature",    difficulty = "MEDIUM"),
            WordEntity(word = "river",    category = "Nature",    difficulty = "MEDIUM"),
            WordEntity(word = "forest",   category = "Nature",    difficulty = "MEDIUM"),
            WordEntity(word = "thunder",  category = "Nature",    difficulty = "MEDIUM"),
            WordEntity(word = "rainbow",  category = "Nature",    difficulty = "MEDIUM"),
            WordEntity(word = "season",   category = "Nature",    difficulty = "MEDIUM"),
            WordEntity(word = "cloud",    category = "Nature",    difficulty = "MEDIUM"),
            WordEntity(word = "desert",   category = "Nature",    difficulty = "MEDIUM"),
            WordEntity(word = "island",   category = "Nature",    difficulty = "MEDIUM"),
            WordEntity(word = "volcano",  category = "Nature",    difficulty = "MEDIUM"),

            // ─── MEDIUM – School ──────────────────────────────────────────────
            WordEntity(word = "pencil",   category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "notebook", category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "teacher",  category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "student",  category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "library",  category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "eraser",   category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "lesson",   category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "science",  category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "english",  category = "School",    difficulty = "MEDIUM"),
            WordEntity(word = "history",  category = "School",    difficulty = "MEDIUM"),

            // ─── HARD ─────────────────────────────────────────────────────────
            WordEntity(word = "crocodile",     category = "Animals",   difficulty = "HARD"),
            WordEntity(word = "rhinoceros",    category = "Animals",   difficulty = "HARD"),
            WordEntity(word = "chimpanzee",    category = "Animals",   difficulty = "HARD"),
            WordEntity(word = "hippopotamus",  category = "Animals",   difficulty = "HARD"),
            WordEntity(word = "chameleon",     category = "Animals",   difficulty = "HARD"),
            WordEntity(word = "archaeology",   category = "Knowledge", difficulty = "HARD"),
            WordEntity(word = "microscope",    category = "Knowledge", difficulty = "HARD"),
            WordEntity(word = "geography",     category = "Knowledge", difficulty = "HARD"),
            WordEntity(word = "encyclopedia",  category = "Knowledge", difficulty = "HARD"),
            WordEntity(word = "atmosphere",    category = "Knowledge", difficulty = "HARD"),
            WordEntity(word = "temperature",   category = "Knowledge", difficulty = "HARD"),
            WordEntity(word = "celebration",   category = "Knowledge", difficulty = "HARD"),
            WordEntity(word = "neighborhood",  category = "Knowledge", difficulty = "HARD"),
            WordEntity(word = "communication", category = "Knowledge", difficulty = "HARD"),
            WordEntity(word = "imagination",   category = "Knowledge", difficulty = "HARD")
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

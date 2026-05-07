package com.example.tinytone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    private val _currentWord = MutableLiveData<WordEntity>()
    val currentWord: LiveData<WordEntity> get() = _currentWord

    /**
     * Fetch the next word to practise.
     *
     * Priority:
     *  1. If a category is specified → pick from that category only.
     *  2. Otherwise adaptive picker: 40% chance to resurface a weak word (score < 75).
     *  3. Otherwise pick by difficulty.
     *  4. Final fallback: any word.
     *
     *  The result is always different from the last shown word (when possible).
     */
    fun fetchNextWord(
        isAdaptivePicker: Boolean,
        selectedDifficulty: String,
        challengeWordId: Int?,
        category: String = ""
    ) {
        viewModelScope.launch {
            val lastId = _currentWord.value?.id ?: -1
            var word: WordEntity? = null

            // 1. Category filter
            if (category.isNotBlank()) {
                repeat(3) {   // try up to 3 times to get a different word
                    if (word == null || word!!.id == lastId) {
                        word = repository.getRandomWordByCategory(category, selectedDifficulty)
                    }
                }
                // If category+difficulty yields nothing, fall back to category alone
                if (word == null || word!!.id == lastId) {
                    word = repository.getRandomWordByCategory(category, "")
                }
            }

            // 2. Adaptive: resurface weak words
            if (word == null && isAdaptivePicker && Math.random() < 0.40) {
                word = repository.getWeakWord()
                if (word?.id == lastId) word = null
            }

            // 3. Difficulty-based pick
            if (word == null) {
                repeat(3) {
                    if (word == null || word!!.id == lastId) {
                        word = repository.getRandomWordByDifficulty(selectedDifficulty)
                    }
                }
            }

            // 4. Absolute fallback
            if (word == null) word = repository.getRandomWord()

            word?.let { _currentWord.postValue(it) }
        }
    }

    fun submitScore(wordId: Int, finalScore: Int, durationMs: Long, difficulty: String) {
        viewModelScope.launch {
            repository.updateWordScore(wordId, finalScore)
            repository.insertSession(
                SessionEntity(
                    wordId        = wordId,
                    accuracyScore = finalScore,
                    durationMs    = durationMs,
                    timestamp     = System.currentTimeMillis(),
                    difficulty    = difficulty
                )
            )
        }
    }
}

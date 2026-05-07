package com.example.tinytone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    private val _currentWord = MutableLiveData<WordEntity>()
    val currentWord: LiveData<WordEntity> get() = _currentWord

    fun fetchNextWord(
        isAdaptivePicker: Boolean,
        selectedDifficulty: String,
        challengeWordId: Int?,
        category: String = ""
    ) {
        viewModelScope.launch {
            val lastId = _currentWord.value?.id ?: -1
            var word: WordEntity? = null

            // 1. Category filter - Strictly respect category if provided
            if (category.isNotBlank()) {
                repeat(3) {
                    if (word == null || word!!.id == lastId) {
                        word = repository.getRandomWordByCategory(category, selectedDifficulty)
                    }
                }
                if (word == null || word!!.id == lastId) {
                    word = repository.getRandomWordByCategory(category, "")
                }
                
                // If we found a word in the category, we post it and stop here.
                if (word != null) {
                    _currentWord.postValue(word!!)
                    return@launch
                }
            }

            // 2. Adaptive: resurface weak words (only if not in a specific category)
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

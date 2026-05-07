package com.example.tinytone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: AppRepository) : ViewModel() {

    private val _totalSessions = MutableLiveData<Int>()
    val totalSessions: LiveData<Int> get() = _totalSessions

    private val _timeSpent = MutableLiveData<Long>()
    val timeSpent: LiveData<Long> get() = _timeSpent

    private val _avgAccuracyEasy = MutableLiveData<Float?>()
    val avgAccuracyEasy: LiveData<Float?> get() = _avgAccuracyEasy

    private val _avgAccuracyMedium = MutableLiveData<Float?>()
    val avgAccuracyMedium: LiveData<Float?> get() = _avgAccuracyMedium

    private val _avgAccuracyHard = MutableLiveData<Float?>()
    val avgAccuracyHard: LiveData<Float?> get() = _avgAccuracyHard

    fun loadParentDashboardStats() {
        viewModelScope.launch {
            _totalSessions.value = repository.getTotalSessions()
            _timeSpent.value = repository.getTotalTimeSpent() ?: 0L
            _avgAccuracyEasy.value = repository.getAverageAccuracyForDifficulty("EASY")
            _avgAccuracyMedium.value = repository.getAverageAccuracyForDifficulty("MEDIUM")
            _avgAccuracyHard.value = repository.getAverageAccuracyForDifficulty("HARD")
        }
    }
}

package com.example.tinytone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProgressViewModel(private val repository: AppRepository) : ViewModel() {

    private val _badges = MutableLiveData<List<BadgeEntity>>()
    val badges: LiveData<List<BadgeEntity>> get() = _badges

    private val _recentSessions = MutableLiveData<List<SessionEntity>>()
    val recentSessions: LiveData<List<SessionEntity>> get() = _recentSessions

    fun loadProgressData() {
        viewModelScope.launch {
            _badges.value = repository.getAllBadges()
            
            // Get last 7 days of sessions
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            _recentSessions.value = repository.getSessionsSince(sevenDaysAgo)
        }
    }
}

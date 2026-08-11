package com.indolearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indolearn.data.local.PreferencesManager
import com.indolearn.data.local.entity.UserProgressEntity
import com.indolearn.data.repository.LearnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LearnRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _progress = MutableStateFlow(UserProgressEntity())
    val progress: StateFlow<UserProgressEntity> = _progress.asStateFlow()

    private val _currentLanguage = MutableStateFlow("ID")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.seedInitialData()
                repository.checkAndUpdateStreak()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            repository.getUserProgress()
                .catch { e -> e.printStackTrace() }
                .collect { if (it != null) _progress.value = it }
        }

        viewModelScope.launch {
            preferencesManager.selectedLanguage.collect { lang ->
                _currentLanguage.value = lang
            }
        }
    }

    fun switchLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.setSelectedLanguage(lang)
        }
    }
}

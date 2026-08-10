package com.indolearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: LearnRepository
) : ViewModel() {

    private val _progress = MutableStateFlow(UserProgressEntity())
    val progress: StateFlow<UserProgressEntity> = _progress.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.seedInitialData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            repository.getUserProgress()
                .catch { e -> e.printStackTrace() }
                .collect { if (it != null) _progress.value = it }
        }
    }
}

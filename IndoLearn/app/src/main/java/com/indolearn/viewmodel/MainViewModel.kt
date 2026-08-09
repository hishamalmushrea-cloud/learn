package com.indolearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indolearn.IndoLearnApp
import com.indolearn.data.local.entity.*
import com.indolearn.data.repository.LearnRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = LearnRepository(IndoLearnApp.database)

    val lessons = MutableStateFlow<List<LessonEntity>>(emptyList())
    val vocabulary = MutableStateFlow<List<VocabularyEntity>>(emptyList())
    val progress = MutableStateFlow<UserProgressEntity>(UserProgressEntity())

    init {
        viewModelScope.launch {
            repository.seedInitialData()
            repository.getLessons(0).collect { lessons.value = it }
            repository.getAllVocabulary().collect { vocabulary.value = it }
            repository.getUserProgress().collect { progress.value = it }
        }
    }

    fun markLessonDone(id: Int) {
        viewModelScope.launch {
            repository.markLessonCompleted(id)
        }
    }

    fun toggleFavorite(id: Int, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, isFav)
        }
    }
}
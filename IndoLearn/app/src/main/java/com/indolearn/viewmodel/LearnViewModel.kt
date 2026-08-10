package com.indolearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indolearn.data.local.entity.CasualExpressionEntity
import com.indolearn.data.local.entity.GrammarEntity
import com.indolearn.data.local.entity.LessonEntity
import com.indolearn.data.local.entity.LessonDetailEntity
import com.indolearn.data.local.entity.TrainingItemEntity
import com.indolearn.data.local.entity.VocabularyEntity
import com.indolearn.data.repository.LearnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val repository: LearnRepository
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<LessonEntity>>(emptyList())
    val lessons: StateFlow<List<LessonEntity>> = _lessons.asStateFlow()

    private val _vocabulary = MutableStateFlow<List<VocabularyEntity>>(emptyList())
    val vocabulary: StateFlow<List<VocabularyEntity>> = _vocabulary.asStateFlow()

    private val _grammar = MutableStateFlow<List<GrammarEntity>>(emptyList())
    val grammar: StateFlow<List<GrammarEntity>> = _grammar.asStateFlow()

    private val _favorites = MutableStateFlow<List<VocabularyEntity>>(emptyList())
    val favorites: StateFlow<List<VocabularyEntity>> = _favorites.asStateFlow()

    private val _casual = MutableStateFlow<List<CasualExpressionEntity>>(emptyList())
    val casual: StateFlow<List<CasualExpressionEntity>> = _casual.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getLessons(0).catch { e -> e.printStackTrace() }
                .collect { _lessons.value = it }
        }
        viewModelScope.launch {
            repository.getAllVocabulary().catch { e -> e.printStackTrace() }
                .collect { _vocabulary.value = it }
        }
        viewModelScope.launch {
            repository.getGrammar(0).catch { e -> e.printStackTrace() }
                .collect { _grammar.value = it }
        }
        viewModelScope.launch {
            repository.getFavorites().catch { e -> e.printStackTrace() }
                .collect { _favorites.value = it }
        }
    }

    fun markLessonDone(id: Int) {
        viewModelScope.launch {
            try {
                repository.markLessonCompleted(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFavorite(id: Int, isFav: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(id, isFav)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getLessonById(id: Int): LessonEntity? {
        return try {
            repository.getLessonById(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getLessonDetail(id: Int): LessonDetailEntity? {
        return try {
            repository.getLessonDetail(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getRandomQuizzes(limit: Int = 10): List<TrainingItemEntity> {
        return try {
            repository.getRandomQuizzes(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

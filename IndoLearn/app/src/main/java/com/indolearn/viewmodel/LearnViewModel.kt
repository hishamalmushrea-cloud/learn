package com.indolearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indolearn.data.local.PreferencesManager
import com.indolearn.data.local.entity.*
import com.indolearn.data.repository.LearnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val repository: LearnRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow("ID")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

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

    private val _dialogues = MutableStateFlow<List<DialogueEntity>>(emptyList())
    val dialogues: StateFlow<List<DialogueEntity>> = _dialogues.asStateFlow()

    private val _stages = MutableStateFlow<List<StageEntity>>(emptyList())
    val stages: StateFlow<List<StageEntity>> = _stages.asStateFlow()

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    private var lessonsJob: Job? = null
    private var vocabJob: Job? = null
    private var grammarJob: Job? = null
    private var favoritesJob: Job? = null
    private var dialoguesJob: Job? = null
    private var stagesJob: Job? = null

    init {
        // Collect current language preferences and bind reactive flows
        viewModelScope.launch {
            preferencesManager.selectedLanguage.collect { lang ->
                _currentLanguage.value = lang
                loadAllDataForLanguage(lang)
            }
        }

        // Notes are language-independent and shared globally
        viewModelScope.launch {
            repository.getAllNotes().catch { e -> e.printStackTrace() }
                .collect { _notes.value = it }
        }
    }

    fun loadAllDataForLanguage(lang: String) {
        lessonsJob?.cancel()
        lessonsJob = viewModelScope.launch {
            repository.getLessons(0, lang).catch { e -> e.printStackTrace() }
                .collect { _lessons.value = it }
        }

        vocabJob?.cancel()
        vocabJob = viewModelScope.launch {
            repository.getAllVocabulary(lang).catch { e -> e.printStackTrace() }
                .collect { _vocabulary.value = it }
        }

        grammarJob?.cancel()
        grammarJob = viewModelScope.launch {
            repository.getGrammar(0, lang).catch { e -> e.printStackTrace() }
                .collect { _grammar.value = it }
        }

        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            repository.getFavorites(lang).catch { e -> e.printStackTrace() }
                .collect { _favorites.value = it }
        }

        dialoguesJob?.cancel()
        dialoguesJob = viewModelScope.launch {
            repository.getAllDialogues(lang).catch { e -> e.printStackTrace() }
                .collect { _dialogues.value = it }
        }

        stagesJob?.cancel()
        stagesJob = viewModelScope.launch {
            repository.getAllStages(lang).catch { e -> e.printStackTrace() }
                .collect { _stages.value = it }
        }
    }

    fun switchLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.setSelectedLanguage(lang)
        }
    }

    fun loadLessonsForLevel(level: Int) {
        lessonsJob?.cancel()
        lessonsJob = viewModelScope.launch {
            repository.getLessons(level, _currentLanguage.value).catch { e -> e.printStackTrace() }
                .collect { _lessons.value = it }
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

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            try {
                repository.saveNote(NoteEntity(title = title, content = content))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

package com.indolearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indolearn.data.local.PreferencesManager
import com.indolearn.data.local.entity.UserProgressEntity
import com.indolearn.data.repository.LearnRepository
import com.indolearn.data.repository.SeedManager
import com.indolearn.domain.srs.MasteryState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/** توزيع حالات الإتقان — يُعرض في شاشة التقدم. */
data class MasteryBreakdown(
    val newCount: Int = 0,
    val learning: Int = 0,
    val weak: Int = 0,
    val forgotten: Int = 0,
    val mastered: Int = 0
) {
    val total: Int get() = newCount + learning + weak + forgotten + mastered
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LearnRepository,
    private val preferencesManager: PreferencesManager,
    private val seedManager: SeedManager
) : ViewModel() {

    private val _progress = MutableStateFlow(UserProgressEntity())
    val progress: StateFlow<UserProgressEntity> = _progress.asStateFlow()

    private val _currentLanguage = MutableStateFlow("ID")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _masteryBreakdown = MutableStateFlow(MasteryBreakdown())
    val masteryBreakdown: StateFlow<MasteryBreakdown> = _masteryBreakdown.asStateFlow()

    private val _dueCount = MutableStateFlow(0)

    /** عدد العناصر المستحقة للمراجعة الآن — يُعرض كشارة في الرئيسية. */
    val dueCount: StateFlow<Int> = _dueCount.asStateFlow()

    private var masteryJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                // بذر ذرّي مضمون مرة واحدة (انظر SeedManager).
                seedManager.ensureSeeded()
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
            preferencesManager.selectedLanguage.collectLatest { lang ->
                _currentLanguage.value = lang
                // user_progress صف ملخّص واحد؛ أعد حسابه فور تبديل اللغة كي
                // لا يرى متعلم التركية أرقام الإندونيسية (والعكس).
                seedManager.ensureSeeded()
                repository.recomputeProgress(lang)
                observeMastery(lang)
            }
        }
    }

    private fun observeMastery(lang: String) {
        // إلغاء مراقبة اللغة السابقة؛ وإلا يستمر Flow القديم في تحديث
        // الأرقام بعد التبديل فتقفز الواجهة بين اللغتين.
        masteryJob?.cancel()
        masteryJob = viewModelScope.launch {
            repository.getReviewStates(lang)
                .catch { e -> e.printStackTrace() }
                .collect { states ->
                    _masteryBreakdown.value = MasteryBreakdown(
                        newCount = states.count { it.mastery == MasteryState.NEW },
                        learning = states.count { it.mastery == MasteryState.LEARNING },
                        weak = states.count { it.mastery == MasteryState.WEAK },
                        forgotten = states.count { it.mastery == MasteryState.FORGOTTEN },
                        mastered = states.count { it.mastery == MasteryState.MASTERED }
                    )
                    val now = System.currentTimeMillis()
                    _dueCount.value = states.count { it.isDue(now) }
                }
        }
    }

    fun switchLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.setSelectedLanguage(lang)
        }
    }

    /** يعيد احتساب التقدم — يُستدعى عند العودة إلى الرئيسية. */
    fun refreshProgress() {
        viewModelScope.launch {
            try {
                repository.recomputeProgress(_currentLanguage.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

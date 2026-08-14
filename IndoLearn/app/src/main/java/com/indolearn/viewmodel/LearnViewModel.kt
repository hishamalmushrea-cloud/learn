package com.indolearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indolearn.data.local.PreferencesManager
import com.indolearn.data.local.entity.*
import com.indolearn.data.repository.LearnRepository
import com.indolearn.data.repository.SeedManager
import com.indolearn.domain.coach.DailyCoach
import com.indolearn.domain.coach.DailySession
import com.indolearn.domain.coach.TaskType
import com.indolearn.domain.srs.Grade
import com.indolearn.domain.srs.ItemKind
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
    private val preferencesManager: PreferencesManager,
    private val seedManager: SeedManager
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

    private val _lessonCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())

    /** عدد الدروس لكل مستوى — تستخدمه شاشة المنهج لعرض المراحل الفارغة بصدق. */
    val lessonCounts: StateFlow<Map<Int, Int>> = _lessonCounts.asStateFlow()

    private val _scenarios = MutableStateFlow<List<DailyScenarioEntity>>(emptyList())
    val scenarios: StateFlow<List<DailyScenarioEntity>> = _scenarios.asStateFlow()

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    private var lessonsJob: Job? = null
    private var vocabJob: Job? = null
    private var grammarJob: Job? = null
    private var favoritesJob: Job? = null
    private var dialoguesJob: Job? = null
    private var stagesJob: Job? = null
    private var casualJob: Job? = null
    private var scenariosJob: Job? = null

    // ⚠️ يجب أن تُعرَّف هذه الحقول **قبل** كتلة init.
    // كتلة init تستدعي loadAllDataForLanguage التي تكتب فيها، وفي Kotlin
    // تُهيّأ الخصائص بترتيب ظهورها؛ لو بقيت أسفل init لكانت null وقت الاستخدام
    // وأدت إلى NullPointerException عند أول تشغيل.
    /**
     * هل فشل بناء جلسة اليوم؟
     *
     * بدون هذه الحالة كان الاستثناء يُبتلع في `catch` فتبقى `_session`
     * فارغة، وشاشة المراجعة تعرض دوّامة **إلى الأبد** بلا أي تفسير —
     * نفس صنف العطل الذي أبلغ عنه المستخدم في شاشات المحتوى.
     */
    private val _sessionFailed = MutableStateFlow(false)
    val sessionFailed: StateFlow<Boolean> = _sessionFailed.asStateFlow()

    private val _session = MutableStateFlow<DailySession?>(null)

    /** جلسة اليوم المبنية على بيانات المستخدم الحقيقية. */
    val session: StateFlow<DailySession?> = _session.asStateFlow()

    private val _reviewQueue = MutableStateFlow<List<VocabularyEntity>>(emptyList())

    /** قائمة العناصر المستحقة فعلاً للمراجعة الآن. */
    val reviewQueue: StateFlow<List<VocabularyEntity>> = _reviewQueue.asStateFlow()


    private val _isReady = MutableStateFlow(false)

    /**
     * هل انتهى تجهيز البيانات؟
     *
     * تفرّق الشاشات بين «جارٍ التحميل» و«لا توجد بيانات».
     * قبل الإصلاح كانت كل شاشة تعرض دوّامة لمجرد أن القائمة فارغة،
     * فتدور إلى الأبد إذا كان الجدول فارغاً فعلاً.
     */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        // Collect current language preferences and bind reactive flows
        viewModelScope.launch {
            // ⚠️ لا تقرأ قبل اكتمال البذر.
            // كانت هذه الشاشات تقرأ فوراً بينما البذر يجري في HomeViewModel
            // (أو لم يجرِ أصلاً)، فتقرأ جداول فارغة وتبقى الدوّامة تدور.
            try {
                seedManager.ensureSeeded()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            preferencesManager.selectedLanguage.collect { lang ->
                _currentLanguage.value = lang
                loadAllDataForLanguage(lang)
                _isReady.value = true
            }
        }

        // Notes are language-independent and shared globally
        viewModelScope.launch {
            repository.getAllNotes().catch { e -> e.printStackTrace() }
                .collect { _notes.value = it }
        }
    }

    fun loadAllDataForLanguage(lang: String) {
        // تفريغ الحالة المشتقة من اللغة السابقة فوراً.
        // بدون ذلك يرى المستخدم — للحظة بعد تبديل اللغة — بطاقات مراجعة
        // وجلسة يومية تخص اللغة القديمة، وقد يقيّم عنصراً بلغة خاطئة.
        _session.value = null
        _reviewQueue.value = emptyList()

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

        // التعبيرات اليومية والسيناريوهات — كانت غير مربوطة إطلاقاً،
        // فكانت CasualScreen تعرض قائمة ثابتة داخل الكود بدل بيانات القاعدة.
        casualJob?.cancel()
        casualJob = viewModelScope.launch {
            repository.getCasualExpressions(lang).catch { e -> e.printStackTrace() }
                .collect { _casual.value = it }
        }

        scenariosJob?.cancel()
        scenariosJob = viewModelScope.launch {
            repository.getScenarios(lang).catch { e -> e.printStackTrace() }
                .collect { _scenarios.value = it }
        }

        viewModelScope.launch {
            try {
                _lessonCounts.value = repository.getLessonCountsByLevel(lang)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        refreshDailySession()
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
                repository.markLessonCompleted(id, _currentLanguage.value)
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
            repository.getRandomQuizzes(limit, _currentLanguage.value)
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

    // ================= Spaced repetition & daily coach =================

    /**
     * يبني جلسة اليوم.
     *
     * قبل الإصلاح كانت `ReviewScreen` تعرض النص الثابت
     * "5 كلمات + 2 قواعد + 1 محادثة" — رقم مختلق غير مرتبط بأي بيانات.
     */
    fun refreshDailySession() {
        viewModelScope.launch {
            try {
                _sessionFailed.value = false
                val lang = _currentLanguage.value
                val states = repository.getReviewStatesOnce(lang)
                val incomplete = repository.getIncompleteLessonIds(lang)
                val scenarios = repository.getScenarioIds(lang)
                val built = DailyCoach.buildSession(
                    states = states,
                    availableNewLessonIds = incomplete,
                    availableScenarioIds = scenarios,
                    now = System.currentTimeMillis()
                )
                _session.value = built

                val dueIds = built.tasks
                    .filter { it.type != TaskType.NEW_LESSON && it.type != TaskType.SCENARIO_PRACTICE }
                    .flatMap { it.itemIds }
                _reviewQueue.value = if (dueIds.isEmpty()) {
                    // لا توجد مراجعات مستحقة ⇒ قدّم عناصر جديدة لم تُدرس بعد.
                    val seen = states.map { it.itemId }.toSet()
                    _vocabulary.value.filter { it.id !in seen }.take(NEW_ITEMS_PER_SESSION)
                } else {
                    repository.getVocabularyByIds(dueIds)
                }
            } catch (e: Exception) {
                // لا نبتلع الفشل صامتين: نُعلم الواجهة كي تعرض رسالة
                // وزر إعادة محاولة بدل دوّامة أبدية.
                e.printStackTrace()
                _sessionFailed.value = true
            }
        }
    }

    /** يسجّل تقييم المستخدم لبطاقة ويعيد جدولتها عبر محرك SM-2. */
    fun gradeItem(itemId: Int, grade: Grade, kind: ItemKind = ItemKind.WORD) {
        viewModelScope.launch {
            try {
                repository.recordReview(itemId, kind, _currentLanguage.value, grade)
                repository.recomputeProgress(_currentLanguage.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** يحفظ نتيجة الاختبار — لم تكن تُحفظ إطلاقاً قبل الإصلاح. */
    fun saveQuizResult(lessonId: Int, score: Int, total: Int) {
        viewModelScope.launch {
            try {
                repository.saveQuizResult(lessonId, score, total)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private companion object {
        const val NEW_ITEMS_PER_SESSION = 10
    }
}

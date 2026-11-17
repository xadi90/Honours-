package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AnnouncementEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.ExamResultEntity
import com.example.data.local.McqEntity
import com.example.data.local.PaperEntity
import com.example.data.local.QuestionAnswerEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.StudyGoalEntity
import com.example.data.local.StudyMaterialEntity
import com.example.data.local.SuggestionEntity
import com.example.data.prefs.AiSettingsState
import com.example.data.prefs.NotificationPrefs
import com.example.data.prefs.StudentProfileState
import com.example.data.prefs.StudyPlannerPrefs
import com.example.data.repository.HonoursRepository
import com.example.data.repository.SearchResultBundle
import com.example.ui.theme.AppThemeMode
import com.example.util.NetworkMonitor
import com.example.util.PdfExporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = HonoursRepository(application.applicationContext)

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val isOnline: StateFlow<Boolean> = NetworkMonitor.observeIsOnline(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val themeMode: StateFlow<AppThemeMode> = repository.prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.SYSTEM)

    val studentProfile: StateFlow<StudentProfileState> = repository.prefs.studentProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentProfileState())

    val notificationPrefs: StateFlow<NotificationPrefs> = repository.prefs.notificationPrefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationPrefs())

    val aiSettings: StateFlow<AiSettingsState> = repository.prefs.aiSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiSettingsState())

    val studyPlannerPrefs: StateFlow<StudyPlannerPrefs> = repository.prefs.studyPlannerPrefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudyPlannerPrefs())

    val allPapers: StateFlow<List<PaperEntity>> = repository.observeAllPapers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChapters: StateFlow<List<ChapterEntity>> = repository.observeAllChapters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuestions: StateFlow<List<QuestionAnswerEntity>> = repository.observeAllQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedQuestions: StateFlow<List<QuestionAnswerEntity>> = repository.observeBookmarkedQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedOfflineQuestions: StateFlow<List<QuestionAnswerEntity>> = repository.observeSavedOfflineQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pastExamQuestions: StateFlow<List<QuestionAnswerEntity>> = repository.observePastExamQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudyMaterials: StateFlow<List<StudyMaterialEntity>> = repository.observeAllStudyMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMaterials: StateFlow<List<StudyMaterialEntity>> = repository.observeRecentMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineMaterials: StateFlow<List<StudyMaterialEntity>> = repository.observeOfflineMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestions: StateFlow<List<SuggestionEntity>> = repository.observeSuggestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.observeAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyGoals: StateFlow<List<StudyGoalEntity>> = repository.observeStudyGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val examResults: StateFlow<List<ExamResultEntity>> = repository.observeExamResults()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.observeSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(SearchResultBundle())
    val searchResults: StateFlow<SearchResultBundle> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _offlineStorageKb = MutableStateFlow(0L)
    val offlineStorageKb: StateFlow<Long> = _offlineStorageKb.asStateFlow()

    init {
        refreshStorageUsage()
    }

    fun refreshStorageUsage() {
        _offlineStorageKb.value = PdfExporter.calculateOfflineStorageKb(getApplication())
    }

    fun observePapersByYear(year: Int): Flow<List<PaperEntity>> = repository.observePapersByYear(year)
    fun observePaperById(paperId: Long): Flow<PaperEntity?> = repository.observePaperById(paperId)
    fun observeChaptersByPaper(paperId: Long): Flow<List<ChapterEntity>> = repository.observeChaptersByPaper(paperId)
    fun observeChapterById(chapterId: Long): Flow<ChapterEntity?> = repository.observeChapterById(chapterId)
    fun observeQuestionsByChapter(chapterId: Long): Flow<List<QuestionAnswerEntity>> = repository.observeQuestionsByChapter(chapterId)
    fun observeQuestionsByPaper(paperId: Long): Flow<List<QuestionAnswerEntity>> = repository.observeQuestionsByPaper(paperId)
    fun observeMcqsByChapter(chapterId: Long): Flow<List<McqEntity>> = repository.observeMcqsByChapter(chapterId)
    fun observeMaterialById(id: Long): Flow<StudyMaterialEntity?> = repository.observeMaterialById(id)

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { repository.prefs.setThemeMode(mode) }
    }

    fun updateProfile(name: String, year: Int, college: String, email: String, isGuest: Boolean) {
        viewModelScope.launch { repository.prefs.updateProfile(name, year, college, email, isGuest) }
    }

    fun updateAvatarUri(uri: String) {
        viewModelScope.launch { repository.prefs.updateAvatarUri(uri) }
    }

    fun logoutToGuest() {
        viewModelScope.launch { repository.prefs.logoutToGuest() }
    }

    fun updateNotificationPrefs(prefs: NotificationPrefs) {
        viewModelScope.launch { repository.prefs.updateNotificationPrefs(prefs) }
    }

    fun toggleChapterCompleted(chapterId: Long, completed: Boolean) {
        viewModelScope.launch { repository.toggleChapterCompleted(chapterId, completed) }
    }

    fun toggleQuestionBookmark(questionId: Long, bookmarked: Boolean) {
        viewModelScope.launch { repository.toggleQuestionBookmark(questionId, bookmarked) }
    }

    fun toggleQuestionSavedOffline(questionId: Long, saved: Boolean) {
        viewModelScope.launch { repository.toggleQuestionSavedOffline(questionId, saved) }
    }

    fun performSearch(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = SearchResultBundle()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = repository.searchAcademic(query)
            _isSearching.value = false
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }

    fun openMaterial(material: StudyMaterialEntity) {
        viewModelScope.launch {
            repository.openMaterial(material)
            refreshStorageUsage()
        }
    }

    fun toggleMaterialDownload(material: StudyMaterialEntity, download: Boolean) {
        viewModelScope.launch {
            repository.toggleMaterialDownload(material, download)
            refreshStorageUsage()
        }
    }

    fun bookmarkMaterialPage(materialId: Long, page: Int) {
        viewModelScope.launch { repository.bookmarkMaterialPage(materialId, page) }
    }

    // Study Planner
    fun addStudyGoal(titleBn: String, targetMinutes: Int, dayLabelBn: String) {
        if (titleBn.isBlank()) return
        viewModelScope.launch {
            repository.addStudyGoal(
                StudyGoalEntity(
                    titleBn = titleBn.trim(),
                    targetMinutes = targetMinutes.coerceIn(10, 360),
                    dayLabelBn = dayLabelBn
                )
            )
        }
    }

    fun toggleStudyGoal(goal: StudyGoalEntity) {
        viewModelScope.launch {
            val newCompleted = !goal.isCompleted
            repository.updateStudyGoal(
                goal.copy(
                    isCompleted = newCompleted,
                    completedMinutes = if (newCompleted) goal.targetMinutes else 0
                )
            )
        }
    }

    fun deleteStudyGoal(goal: StudyGoalEntity) {
        viewModelScope.launch { repository.deleteStudyGoal(goal) }
    }

    fun updateStudyPlannerPrefs(dailyGoalMinutes: Int, examTitle: String, examTargetMillis: Long) {
        viewModelScope.launch {
            repository.prefs.updateStudyPlannerPrefs(dailyGoalMinutes, examTitle, examTargetMillis)
        }
    }

    // Admin Studio CRUD (Never alters permanent DeveloperConstants)
    fun adminAddPaper(year: Int, code: String, titleEn: String, titleBn: String, credits: Int) {
        if (code.isBlank() || titleBn.isBlank()) return
        viewModelScope.launch {
            repository.adminAddPaper(
                PaperEntity(
                    yearNumber = year,
                    code = code.trim(),
                    titleEn = titleEn.trim().ifBlank { titleBn.trim() },
                    titleBn = titleBn.trim(),
                    credits = credits,
                    marks = 100
                )
            )
        }
    }

    fun adminAddChapter(paperId: Long, number: Int, titleBn: String, titleEn: String, introBn: String, defsBn: String, topicsBn: String) {
        if (titleBn.isBlank()) return
        viewModelScope.launch {
            repository.adminAddChapter(
                ChapterEntity(
                    paperId = paperId,
                    chapterNumber = number,
                    titleBn = titleBn.trim(),
                    titleEn = titleEn.trim(),
                    introductionBn = introBn.trim(),
                    keyDefinitionsBn = defsBn.trim(),
                    importantTopicsBn = topicsBn.trim()
                )
            )
        }
    }

    fun adminAddQuestion(
        paperId: Long,
        chapterId: Long,
        category: String,
        questionBn: String,
        answerBn: String,
        isPastExam: Boolean,
        examYear: String
    ) {
        if (questionBn.isBlank() || answerBn.isBlank()) return
        viewModelScope.launch {
            repository.adminAddQuestion(
                QuestionAnswerEntity(
                    paperId = paperId,
                    chapterId = chapterId,
                    category = category,
                    questionBn = questionBn.trim(),
                    answerBn = answerBn.trim(),
                    isPastExamQuestion = isPastExam,
                    examYear = examYear.trim(),
                    isVerified = true
                )
            )
        }
    }

    fun adminAddSuggestion(year: Int, paperCode: String, paperTitleBn: String, category: String, titleBn: String, itemsBn: String) {
        if (titleBn.isBlank() || itemsBn.isBlank()) return
        viewModelScope.launch {
            repository.adminAddSuggestion(
                SuggestionEntity(
                    yearNumber = year,
                    paperCode = paperCode.trim(),
                    paperTitleBn = paperTitleBn.trim(),
                    category = category,
                    titleBn = titleBn.trim(),
                    itemsTextBn = itemsBn.trim(),
                    publishedDate = "২০২৬"
                )
            )
        }
    }

    fun adminAddAnnouncement(titleBn: String, bodyBn: String, isImportant: Boolean) {
        if (titleBn.isBlank() || bodyBn.isBlank()) return
        viewModelScope.launch {
            repository.adminAddAnnouncement(
                AnnouncementEntity(
                    titleBn = titleBn.trim(),
                    bodyBn = bodyBn.trim(),
                    publishedAt = "আজ",
                    isImportant = isImportant
                )
            )
        }
    }

    fun adminAddMcq(paperId: Long, chapterId: Long, q: String, a: String, b: String, c: String, d: String, correctIdx: Int, exp: String) {
        if (q.isBlank()) return
        viewModelScope.launch {
            repository.adminAddMcq(
                McqEntity(
                    paperId = paperId,
                    chapterId = chapterId,
                    questionBn = q.trim(),
                    optionA = a.trim(),
                    optionB = b.trim(),
                    optionC = c.trim(),
                    optionD = d.trim(),
                    correctIndex = correctIdx.coerceIn(0, 3),
                    explanationBn = exp.trim()
                )
            )
        }
    }

    fun adminUpdateAiSettings(dailyLimit: Int, imageEnabled: Boolean) {
        viewModelScope.launch {
            repository.prefs.updateAiAdminSettings(dailyLimit, imageEnabled)
        }
    }
}

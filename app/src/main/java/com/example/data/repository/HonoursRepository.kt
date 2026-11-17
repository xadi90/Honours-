package com.example.data.repository

import android.content.Context
import com.example.data.ai.AiRepository
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.AnnouncementEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.ExamResultEntity
import com.example.data.local.HonoursDatabase
import com.example.data.local.McqEntity
import com.example.data.local.PaperEntity
import com.example.data.local.QuestionAnswerEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.StudyGoalEntity
import com.example.data.local.StudyMaterialEntity
import com.example.data.local.SuggestionEntity
import com.example.data.local.AiConversationEntity
import com.example.data.local.AiMessageEntity
import com.example.data.prefs.UserPreferencesRepository
import com.example.util.PdfExporter
import kotlinx.coroutines.flow.Flow

data class SearchResultBundle(
    val papers: List<PaperEntity> = emptyList(),
    val chapters: List<ChapterEntity> = emptyList(),
    val questions: List<QuestionAnswerEntity> = emptyList()
) {
    val isEmpty: Boolean get() = papers.isEmpty() && chapters.isEmpty() && questions.isEmpty()
}

class HonoursRepository(
    private val context: Context,
    private val db: HonoursDatabase = HonoursDatabase.getInstance(context),
    val prefs: UserPreferencesRepository = UserPreferencesRepository(context),
    val aiRepository: AiRepository = PlaceholderAiRepository()
) {
    private val academicDao = db.academicDao()
    private val studyDao = db.mcqAndStudyDao()
    private val aiDao = db.aiAndSearchDao()

    suspend fun ensureSeeded() {
        HonoursDatabase.ensureSeeded(db)
    }

    // Academic Years, Papers, Chapters, Q&A
    fun observeAllPapers(): Flow<List<PaperEntity>> = academicDao.observeAllPapers()
    fun observePapersByYear(year: Int): Flow<List<PaperEntity>> = academicDao.observePapersByYear(year)
    fun observePaperById(paperId: Long): Flow<PaperEntity?> = academicDao.observePaperById(paperId)
    fun observeChaptersByPaper(paperId: Long): Flow<List<ChapterEntity>> = academicDao.observeChaptersByPaper(paperId)
    fun observeAllChapters(): Flow<List<ChapterEntity>> = academicDao.observeAllChapters()
    fun observeChapterById(chapterId: Long): Flow<ChapterEntity?> = academicDao.observeChapterById(chapterId)
    fun observeQuestionsByChapter(chapterId: Long): Flow<List<QuestionAnswerEntity>> = academicDao.observeQuestionsByChapter(chapterId)
    fun observeQuestionsByPaper(paperId: Long): Flow<List<QuestionAnswerEntity>> = academicDao.observeQuestionsByPaper(paperId)
    fun observeAllQuestions(): Flow<List<QuestionAnswerEntity>> = academicDao.observeAllQuestions()
    fun observeBookmarkedQuestions(): Flow<List<QuestionAnswerEntity>> = academicDao.observeBookmarkedQuestions()
    fun observeSavedOfflineQuestions(): Flow<List<QuestionAnswerEntity>> = academicDao.observeSavedOfflineQuestions()
    fun observePastExamQuestions(): Flow<List<QuestionAnswerEntity>> = academicDao.observePastExamQuestions()

    suspend fun toggleChapterCompleted(chapterId: Long, completed: Boolean) {
        academicDao.updateChapterCompletion(chapterId, completed)
    }

    suspend fun toggleQuestionBookmark(questionId: Long, bookmarked: Boolean) {
        academicDao.setQuestionBookmarked(questionId, bookmarked)
    }

    suspend fun toggleQuestionSavedOffline(questionId: Long, saved: Boolean) {
        academicDao.setQuestionSavedOffline(questionId, saved)
    }

    // Search
    fun observeSearchHistory(): Flow<List<SearchHistoryEntity>> = aiDao.observeSearchHistory()

    suspend fun searchAcademic(query: String): SearchResultBundle {
        val clean = query.trim()
        if (clean.isBlank()) return SearchResultBundle()
        aiDao.insertSearchHistory(SearchHistoryEntity(query = clean))
        return SearchResultBundle(
            papers = academicDao.searchPapers(clean),
            chapters = academicDao.searchChapters(clean),
            questions = academicDao.searchQuestions(clean)
        )
    }

    suspend fun clearSearchHistory() = aiDao.clearSearchHistory()

    // MCQs & Exams
    fun observeMcqsByChapter(chapterId: Long): Flow<List<McqEntity>> = studyDao.observeMcqsByChapter(chapterId)
    fun observeMcqsByPaper(paperId: Long): Flow<List<McqEntity>> = studyDao.observeMcqsByPaper(paperId)
    fun observeAllMcqs(): Flow<List<McqEntity>> = studyDao.observeAllMcqs()
    fun observeExamResults(): Flow<List<ExamResultEntity>> = studyDao.observeExamResults()
    suspend fun saveExamResult(result: ExamResultEntity) = studyDao.insertExamResult(result)

    // Study Materials & Offline PDFs
    fun observeAllStudyMaterials(): Flow<List<StudyMaterialEntity>> = studyDao.observeAllStudyMaterials()
    fun observeRecentMaterials(): Flow<List<StudyMaterialEntity>> = studyDao.observeRecentMaterials()
    fun observeOfflineMaterials(): Flow<List<StudyMaterialEntity>> = studyDao.observeOfflineMaterials()
    fun observeMaterialById(id: Long): Flow<StudyMaterialEntity?> = studyDao.observeMaterialById(id)

    suspend fun openMaterial(material: StudyMaterialEntity) {
        studyDao.markMaterialOpened(material.id)
        PdfExporter.ensureMaterialPdfExists(
            context = context,
            materialId = material.id,
            title = material.titleBn,
            subtitle = "পত্র কোড: ${material.paperCode} • অনার্স ${material.yearNumber}ম বর্ষ",
            bodyText = material.contentBodyBn
        )
    }

    suspend fun toggleMaterialDownload(material: StudyMaterialEntity, download: Boolean) {
        if (download) {
            PdfExporter.ensureMaterialPdfExists(
                context = context,
                materialId = material.id,
                title = material.titleBn,
                subtitle = "পত্র কোড: ${material.paperCode} • অনার্স ${material.yearNumber}ম বর্ষ",
                bodyText = material.contentBodyBn
            )
        } else {
            PdfExporter.deleteMaterialPdf(context, material.id)
        }
        studyDao.setMaterialDownloaded(material.id, download)
    }

    suspend fun bookmarkMaterialPage(materialId: Long, page: Int) {
        studyDao.updateMaterialBookmarkedPage(materialId, page)
    }

    // Suggestions & Announcements
    fun observeSuggestions(): Flow<List<SuggestionEntity>> = studyDao.observeAllSuggestions()
    fun observeAnnouncements(): Flow<List<AnnouncementEntity>> = studyDao.observeAnnouncements()

    // Study Planner Goals
    fun observeStudyGoals(): Flow<List<StudyGoalEntity>> = studyDao.observeStudyGoals()
    suspend fun addStudyGoal(goal: StudyGoalEntity) = studyDao.insertStudyGoal(goal)
    suspend fun updateStudyGoal(goal: StudyGoalEntity) = studyDao.updateStudyGoal(goal)
    suspend fun deleteStudyGoal(goal: StudyGoalEntity) = studyDao.deleteStudyGoal(goal)

    // AI Conversations
    fun observeAiConversations(): Flow<List<AiConversationEntity>> = aiDao.observeConversations()
    fun observeBookmarkedAiConversations(): Flow<List<AiConversationEntity>> = aiDao.observeBookmarkedConversations()
    fun observeAiMessages(conversationId: Long): Flow<List<AiMessageEntity>> = aiDao.observeMessages(conversationId)

    suspend fun createAiConversation(
        titleBn: String,
        yearNumber: Int,
        paperTitleBn: String,
        answerLength: String
    ): Long {
        return aiDao.insertConversation(
            AiConversationEntity(
                titleBn = titleBn.take(80),
                yearNumber = yearNumber,
                paperTitleBn = paperTitleBn,
                answerLength = answerLength
            )
        )
    }

    suspend fun appendAiMessage(conversationId: Long, role: String, content: String, imageCount: Int = 0) {
        aiDao.insertMessage(
            AiMessageEntity(
                conversationId = conversationId,
                role = role,
                contentText = content,
                imageCount = imageCount
            )
        )
        aiDao.touchConversation(conversationId)
    }

    suspend fun toggleAiConversationBookmark(conversationId: Long, bookmarked: Boolean) {
        aiDao.setConversationBookmarked(conversationId, bookmarked)
    }

    suspend fun deleteAiConversation(conversationId: Long) {
        aiDao.deleteMessagesForConversation(conversationId)
        aiDao.deleteConversation(conversationId)
    }

    // Admin Content Management Operations (Role-validated in ViewModel)
    suspend fun adminAddPaper(paper: PaperEntity) = academicDao.insertPaper(paper)
    suspend fun adminDeletePaper(paper: PaperEntity) = academicDao.deletePaper(paper)
    suspend fun adminAddChapter(chapter: ChapterEntity) = academicDao.insertChapter(chapter)
    suspend fun adminAddQuestion(qa: QuestionAnswerEntity) = academicDao.insertQuestion(qa)
    suspend fun adminAddMcq(mcq: McqEntity) = studyDao.insertMcq(mcq)
    suspend fun adminAddSuggestion(suggestion: SuggestionEntity) = studyDao.insertSuggestion(suggestion)
    suspend fun adminAddAnnouncement(announcement: AnnouncementEntity) = studyDao.insertAnnouncement(announcement)
    suspend fun adminAddStudyMaterial(material: StudyMaterialEntity) = studyDao.insertStudyMaterial(material)
}

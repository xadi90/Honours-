package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicDao {
    @Query("SELECT * FROM papers ORDER BY yearNumber ASC, code ASC")
    fun observeAllPapers(): Flow<List<PaperEntity>>

    @Query("SELECT * FROM papers WHERE yearNumber = :year ORDER BY code ASC")
    fun observePapersByYear(year: Int): Flow<List<PaperEntity>>

    @Query("SELECT * FROM papers WHERE id = :paperId")
    fun observePaperById(paperId: Long): Flow<PaperEntity?>

    @Query("SELECT * FROM papers WHERE id = :paperId")
    suspend fun getPaperById(paperId: Long): PaperEntity?

    @Query("SELECT COUNT(*) FROM papers")
    suspend fun countPapers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: PaperEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPapers(papers: List<PaperEntity>): List<Long>

    @Delete
    suspend fun deletePaper(paper: PaperEntity)

    @Query("SELECT * FROM chapters WHERE paperId = :paperId ORDER BY chapterNumber ASC")
    fun observeChaptersByPaper(paperId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY paperId ASC, chapterNumber ASC")
    fun observeAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    fun observeChapterById(chapterId: Long): Flow<ChapterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>): List<Long>

    @Query("UPDATE chapters SET isCompleted = :completed WHERE id = :chapterId")
    suspend fun updateChapterCompletion(chapterId: Long, completed: Boolean)

    @Query("SELECT * FROM questions_answers WHERE chapterId = :chapterId ORDER BY id ASC")
    fun observeQuestionsByChapter(chapterId: Long): Flow<List<QuestionAnswerEntity>>

    @Query("SELECT * FROM questions_answers WHERE paperId = :paperId ORDER BY id ASC")
    fun observeQuestionsByPaper(paperId: Long): Flow<List<QuestionAnswerEntity>>

    @Query("SELECT * FROM questions_answers ORDER BY id DESC")
    fun observeAllQuestions(): Flow<List<QuestionAnswerEntity>>

    @Query("SELECT * FROM questions_answers WHERE isBookmarked = 1 ORDER BY id DESC")
    fun observeBookmarkedQuestions(): Flow<List<QuestionAnswerEntity>>

    @Query("SELECT * FROM questions_answers WHERE isSavedOffline = 1 ORDER BY id DESC")
    fun observeSavedOfflineQuestions(): Flow<List<QuestionAnswerEntity>>

    @Query("SELECT * FROM questions_answers WHERE isPastExamQuestion = 1 ORDER BY examYear DESC, id DESC")
    fun observePastExamQuestions(): Flow<List<QuestionAnswerEntity>>

    @Query("UPDATE questions_answers SET isBookmarked = :bookmarked WHERE id = :questionId")
    suspend fun setQuestionBookmarked(questionId: Long, bookmarked: Boolean)

    @Query("UPDATE questions_answers SET isSavedOffline = :saved WHERE id = :questionId")
    suspend fun setQuestionSavedOffline(questionId: Long, saved: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionAnswerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionAnswerEntity>)

    @Delete
    suspend fun deleteQuestion(question: QuestionAnswerEntity)

    @Query(
        """
        SELECT * FROM papers 
        WHERE titleBn LIKE '%' || :query || '%' 
           OR titleEn LIKE '%' || :query || '%' 
           OR code LIKE '%' || :query || '%'
        ORDER BY yearNumber ASC, code ASC
        """
    )
    suspend fun searchPapers(query: String): List<PaperEntity>

    @Query(
        """
        SELECT * FROM chapters 
        WHERE titleBn LIKE '%' || :query || '%' 
           OR titleEn LIKE '%' || :query || '%' 
           OR importantTopicsBn LIKE '%' || :query || '%'
        ORDER BY chapterNumber ASC
        """
    )
    suspend fun searchChapters(query: String): List<ChapterEntity>

    @Query(
        """
        SELECT * FROM questions_answers 
        WHERE questionBn LIKE '%' || :query || '%' 
           OR questionEn LIKE '%' || :query || '%' 
           OR answerBn LIKE '%' || :query || '%'
        ORDER BY isPastExamQuestion DESC, id DESC
        """
    )
    suspend fun searchQuestions(query: String): List<QuestionAnswerEntity>
}

@Dao
interface McqAndStudyDao {
    @Query("SELECT * FROM mcq_questions WHERE chapterId = :chapterId ORDER BY id ASC")
    fun observeMcqsByChapter(chapterId: Long): Flow<List<McqEntity>>

    @Query("SELECT * FROM mcq_questions WHERE paperId = :paperId ORDER BY id ASC")
    fun observeMcqsByPaper(paperId: Long): Flow<List<McqEntity>>

    @Query("SELECT * FROM mcq_questions ORDER BY id ASC")
    fun observeAllMcqs(): Flow<List<McqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMcq(mcq: McqEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMcqs(mcqs: List<McqEntity>)

    @Query("SELECT * FROM exam_results ORDER BY takenAtMillis DESC")
    fun observeExamResults(): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: ExamResultEntity): Long

    @Query("SELECT * FROM study_materials ORDER BY lastOpenedMillis DESC, id ASC")
    fun observeAllStudyMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE lastOpenedMillis > 0 ORDER BY lastOpenedMillis DESC LIMIT 6")
    fun observeRecentMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE isDownloadedOffline = 1 ORDER BY id DESC")
    fun observeOfflineMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE id = :materialId")
    fun observeMaterialById(materialId: Long): Flow<StudyMaterialEntity?>

    @Query("UPDATE study_materials SET lastOpenedMillis = :millis WHERE id = :materialId")
    suspend fun markMaterialOpened(materialId: Long, millis: Long = System.currentTimeMillis())

    @Query("UPDATE study_materials SET isDownloadedOffline = :downloaded WHERE id = :materialId")
    suspend fun setMaterialDownloaded(materialId: Long, downloaded: Boolean)

    @Query("UPDATE study_materials SET bookmarkedPage = :page WHERE id = :materialId")
    suspend fun updateMaterialBookmarkedPage(materialId: Long, page: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterials(materials: List<StudyMaterialEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterialEntity): Long

    @Query("SELECT * FROM suggestions ORDER BY yearNumber ASC, id DESC")
    fun observeAllSuggestions(): Flow<List<SuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestions(suggestions: List<SuggestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: SuggestionEntity): Long

    @Query("SELECT * FROM announcements ORDER BY isImportant DESC, id DESC")
    fun observeAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Query("SELECT * FROM study_goals ORDER BY isCompleted ASC, id DESC")
    fun observeStudyGoals(): Flow<List<StudyGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGoals(goals: List<StudyGoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGoal(goal: StudyGoalEntity): Long

    @Update
    suspend fun updateStudyGoal(goal: StudyGoalEntity)

    @Delete
    suspend fun deleteStudyGoal(goal: StudyGoalEntity)
}

@Dao
interface AiAndSearchDao {
    @Query("SELECT * FROM ai_conversations ORDER BY updatedAtMillis DESC")
    fun observeConversations(): Flow<List<AiConversationEntity>>

    @Query("SELECT * FROM ai_conversations WHERE isBookmarked = 1 ORDER BY updatedAtMillis DESC")
    fun observeBookmarkedConversations(): Flow<List<AiConversationEntity>>

    @Query("SELECT * FROM ai_conversations WHERE id = :conversationId")
    fun observeConversation(conversationId: Long): Flow<AiConversationEntity?>

    @Query("SELECT * FROM ai_messages WHERE conversationId = :conversationId ORDER BY timestampMillis ASC, id ASC")
    fun observeMessages(conversationId: Long): Flow<List<AiMessageEntity>>

    @Query("SELECT * FROM ai_messages WHERE conversationId = :conversationId ORDER BY timestampMillis ASC, id ASC")
    suspend fun getMessagesSnapshot(conversationId: Long): List<AiMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AiConversationEntity): Long

    @Query("UPDATE ai_conversations SET updatedAtMillis = :millis WHERE id = :conversationId")
    suspend fun touchConversation(conversationId: Long, millis: Long = System.currentTimeMillis())

    @Query("UPDATE ai_conversations SET isBookmarked = :bookmarked WHERE id = :conversationId")
    suspend fun setConversationBookmarked(conversationId: Long, bookmarked: Boolean)

    @Query("DELETE FROM ai_conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("DELETE FROM ai_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiMessageEntity): Long

    @Query("SELECT * FROM search_history ORDER BY searchedAtMillis DESC LIMIT 10")
    fun observeSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}

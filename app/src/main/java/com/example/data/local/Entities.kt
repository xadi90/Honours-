package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "papers",
    indices = [Index(value = ["yearNumber"]), Index(value = ["code"])]
)
data class PaperEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearNumber: Int,
    val code: String,
    val titleEn: String,
    val titleBn: String,
    val credits: Int,
    val marks: Int,
    val department: String = "Political Science",
    val isCompulsory: Boolean = true,
    val isElective: Boolean = false
)

@Entity(
    tableName = "chapters",
    indices = [Index(value = ["paperId"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paperId: Long,
    val chapterNumber: Int,
    val titleBn: String,
    val titleEn: String,
    val introductionBn: String,
    val keyDefinitionsBn: String,
    val importantTopicsBn: String,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "questions_answers",
    indices = [Index(value = ["paperId"]), Index(value = ["chapterId"]), Index(value = ["category"])]
)
data class QuestionAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paperId: Long,
    val chapterId: Long,
    val category: String, // QuestionCategory.name
    val questionBn: String,
    val questionEn: String = "",
    val answerBn: String,
    val isBookmarked: Boolean = false,
    val isSavedOffline: Boolean = false,
    val isPastExamQuestion: Boolean = false,
    val examYear: String = "",
    val isVerified: Boolean = true
)

@Entity(
    tableName = "mcq_questions",
    indices = [Index(value = ["paperId"]), Index(value = ["chapterId"])]
)
data class McqEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paperId: Long,
    val chapterId: Long,
    val questionBn: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int, // 0..3
    val explanationBn: String
)

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paperId: Long,
    val paperTitleBn: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val timeTakenSeconds: Long,
    val takenAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_materials",
    indices = [Index(value = ["paperId"]), Index(value = ["yearNumber"])]
)
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paperId: Long,
    val yearNumber: Int,
    val paperCode: String,
    val titleBn: String,
    val type: String, // PDF, NOTE, IMAGE
    val summaryBn: String,
    val contentBodyBn: String,
    val isDownloadedOffline: Boolean = false,
    val fileSizeKb: Int = 120,
    val lastOpenedMillis: Long = 0L,
    val bookmarkedPage: Int = 0,
    val isVerifiedOfficial: Boolean = true
)

@Entity(
    tableName = "suggestions",
    indices = [Index(value = ["yearNumber"]), Index(value = ["category"])]
)
data class SuggestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearNumber: Int,
    val paperCode: String,
    val paperTitleBn: String,
    val category: String, // SuggestionCategory.name
    val titleBn: String,
    val itemsTextBn: String,
    val publishedDate: String
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleBn: String,
    val bodyBn: String,
    val publishedAt: String,
    val isImportant: Boolean = false
)

@Entity(tableName = "study_goals")
data class StudyGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleBn: String,
    val targetMinutes: Int,
    val completedMinutes: Int = 0,
    val dayLabelBn: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "ai_conversations")
data class AiConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleBn: String,
    val yearNumber: Int,
    val paperTitleBn: String,
    val answerLength: String,
    val isBookmarked: Boolean = false,
    val isSavedOffline: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ai_messages",
    indices = [Index(value = ["conversationId"])]
)
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: String, // "user" or "assistant"
    val contentText: String,
    val imageCount: Int = 0,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAtMillis: Long = System.currentTimeMillis()
)

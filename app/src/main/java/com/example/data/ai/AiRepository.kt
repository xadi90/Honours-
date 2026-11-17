package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.model.AiQuickTool
import com.example.data.model.AnswerLengthOption
import com.example.data.model.QuestionCategory
import kotlinx.coroutines.flow.Flow

data class AiSolveRequest(
    val questionText: String,
    val academicYear: Int,
    val paperTitle: String,
    val questionType: QuestionCategory,
    val answerLength: AnswerLengthOption,
    val images: List<Bitmap> = emptyList(),
    val conversationHistory: List<Pair<String, String>> = emptyList()
)

sealed interface AiStreamEvent {
    data object Analyzing : AiStreamEvent
    data class Streaming(val partialText: String) : AiStreamEvent
    data class Completed(val fullText: String, val detectedQuestionText: String? = null) : AiStreamEvent
    data class Error(val message: String, val isUnreadableImage: Boolean = false) : AiStreamEvent
}

interface AiRepository {
    val isLiveGeminiConnected: Boolean
    val noticeBannerBn: String

    fun solveQuestionStream(request: AiSolveRequest): Flow<AiStreamEvent>

    fun applyQuickToolStream(
        previousAnswer: String,
        originalQuestion: String,
        tool: AiQuickTool,
        academicYear: Int,
        paperTitle: String
    ): Flow<AiStreamEvent>
}

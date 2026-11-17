package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiSolveRequest
import com.example.data.ai.AiStreamEvent
import com.example.data.local.AiConversationEntity
import com.example.data.local.AiMessageEntity
import com.example.data.model.AiQuickTool
import com.example.data.model.AnswerLengthOption
import com.example.data.model.QuestionCategory
import com.example.data.repository.HonoursRepository
import com.example.util.ImageUtils
import com.example.util.NetworkMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AiUiStatus {
    data object Idle : AiUiStatus
    data object Loading : AiUiStatus
    data class Streaming(val partialText: String) : AiUiStatus
    data object Ready : AiUiStatus
    data class Error(val message: String, val isUnreadableImage: Boolean = false) : AiUiStatus
}

data class AiSolverUiState(
    val questionInput: String = "",
    val followUpInput: String = "",
    val selectedYear: Int = 1,
    val selectedPaperTitle: String = "রাজনৈতিক প্রতিষ্ঠান ও সংগঠন (211901)",
    val selectedQuestionType: QuestionCategory = QuestionCategory.BROAD,
    val selectedAnswerLength: AnswerLengthOption = AnswerLengthOption.EXAM_READY,
    val attachedImages: List<Bitmap> = emptyList(),
    val activeConversationId: Long? = null,
    val activeConversationTitle: String = "",
    val isCurrentBookmarked: Boolean = false,
    val status: AiUiStatus = AiUiStatus.Idle,
    val lastOriginalQuestion: String = "",
    val lastAnswerText: String = ""
)

class AiSolverViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HonoursRepository(application.applicationContext)
    private val aiRepo = repository.aiRepository

    val noticeBannerBn: String = aiRepo.noticeBannerBn
    val isLiveGeminiConnected: Boolean = aiRepo.isLiveGeminiConnected

    private val _uiState = MutableStateFlow(AiSolverUiState())
    val uiState: StateFlow<AiSolverUiState> = _uiState.asStateFlow()

    val conversations: StateFlow<List<AiConversationEntity>> = repository.observeAiConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMessages: StateFlow<List<AiMessageEntity>> = _uiState
        .flatMapLatest { state ->
            val id = state.activeConversationId
            if (id != null) repository.observeAiMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activeJob: Job? = null

    fun updateQuestionInput(text: String) {
        _uiState.update { it.copy(questionInput = text) }
    }

    fun updateFollowUpInput(text: String) {
        _uiState.update { it.copy(followUpInput = text) }
    }

    fun clearQuestionInput() {
        _uiState.update { it.copy(questionInput = "") }
    }

    fun selectYear(year: Int, defaultPaperTitle: String) {
        _uiState.update { it.copy(selectedYear = year, selectedPaperTitle = defaultPaperTitle) }
    }

    fun selectPaper(paperTitle: String) {
        _uiState.update { it.copy(selectedPaperTitle = paperTitle) }
    }

    fun selectQuestionType(type: QuestionCategory) {
        _uiState.update { it.copy(selectedQuestionType = type) }
    }

    fun selectAnswerLength(length: AnswerLengthOption) {
        _uiState.update { it.copy(selectedAnswerLength = length) }
    }

    fun addImage(bitmap: Bitmap) {
        _uiState.update {
            if (it.attachedImages.size >= 4) it
            else it.copy(attachedImages = it.attachedImages + bitmap)
        }
    }

    fun removeImageAt(index: Int) {
        _uiState.update { state ->
            val updated = state.attachedImages.toMutableList()
            if (index in updated.indices) updated.removeAt(index)
            state.copy(attachedImages = updated)
        }
    }

    fun rotateImageAt(index: Int) {
        _uiState.update { state ->
            val updated = state.attachedImages.toMutableList()
            if (index in updated.indices) {
                updated[index] = ImageUtils.rotateBitmap(updated[index], 90f)
            }
            state.copy(attachedImages = updated)
        }
    }

    fun cropImageAt(index: Int, ratioW: Int? = null, ratioH: Int? = null) {
        _uiState.update { state ->
            val updated = state.attachedImages.toMutableList()
            if (index in updated.indices) {
                updated[index] = if (ratioW != null && ratioH != null) {
                    ImageUtils.cropBitmapCenterRatio(updated[index], ratioW, ratioH)
                } else {
                    ImageUtils.cropBitmapMargins(updated[index], 0.12f)
                }
            }
            state.copy(attachedImages = updated)
        }
    }

    fun startNewSession() {
        activeJob?.cancel()
        _uiState.update {
            it.copy(
                questionInput = "",
                followUpInput = "",
                attachedImages = emptyList(),
                activeConversationId = null,
                activeConversationTitle = "",
                isCurrentBookmarked = false,
                status = AiUiStatus.Idle,
                lastOriginalQuestion = "",
                lastAnswerText = ""
            )
        }
    }

    fun loadConversation(conversation: AiConversationEntity) {
        activeJob?.cancel()
        _uiState.update {
            it.copy(
                activeConversationId = conversation.id,
                activeConversationTitle = conversation.titleBn,
                selectedYear = conversation.yearNumber,
                selectedPaperTitle = conversation.paperTitleBn,
                isCurrentBookmarked = conversation.isBookmarked,
                status = AiUiStatus.Ready
            )
        }
    }

    fun toggleCurrentBookmark() {
        val convId = _uiState.value.activeConversationId ?: return
        val next = !_uiState.value.isCurrentBookmarked
        _uiState.update { it.copy(isCurrentBookmarked = next) }
        viewModelScope.launch {
            repository.toggleAiConversationBookmark(convId, next)
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteAiConversation(id)
            if (_uiState.value.activeConversationId == id) {
                startNewSession()
            }
        }
    }

    fun submitQuestion(isFollowUp: Boolean = false) {
        val state = _uiState.value
        val prompt = if (isFollowUp) state.followUpInput.trim() else state.questionInput.trim()
        val images = if (isFollowUp) emptyList() else state.attachedImages

        if (prompt.isBlank() && images.isEmpty()) {
            _uiState.update { it.copy(status = AiUiStatus.Error("অনুগ্রহ করে প্রশ্ন লিখুন অথবা প্রশ্নের ছবি নির্বাচন করুন।")) }
            return
        }

        if (!NetworkMonitor.isCurrentlyOnline(getApplication()) && aiRepo.isLiveGeminiConnected) {
            _uiState.update {
                it.copy(status = AiUiStatus.Error("ইন্টারনেট সংযোগ নেই। AI সমাধানের জন্য ইন্টারনেট চালু করুন।"))
            }
            return
        }

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            try {
                val historyPairs = if (isFollowUp) {
                    activeMessages.value.map { it.role to it.contentText }
                } else emptyList()

                val convId = if (isFollowUp && state.activeConversationId != null) {
                    state.activeConversationId
                } else {
                    val title = prompt.ifBlank { "ছবি থেকে প্রশ্ন (${state.selectedPaperTitle})" }
                    repository.createAiConversation(
                        titleBn = title,
                        yearNumber = state.selectedYear,
                        paperTitleBn = state.selectedPaperTitle,
                        answerLength = state.selectedAnswerLength.labelBn
                    )
                }

                val userMsgDisplay = if (prompt.isNotBlank()) prompt else "[${images.size}টি প্রশ্নের ছবি আপলোড করা হয়েছে]"
                repository.appendAiMessage(convId, "user", userMsgDisplay, images.size)
                repository.prefs.incrementAiUsage()

                _uiState.update {
                    it.copy(
                        activeConversationId = convId,
                        activeConversationTitle = userMsgDisplay.take(70),
                        lastOriginalQuestion = userMsgDisplay,
                        followUpInput = if (isFollowUp) "" else it.followUpInput,
                        status = AiUiStatus.Loading
                    )
                }

                val request = AiSolveRequest(
                    questionText = prompt,
                    academicYear = state.selectedYear,
                    paperTitle = state.selectedPaperTitle,
                    questionType = state.selectedQuestionType,
                    answerLength = state.selectedAnswerLength,
                    images = images,
                    conversationHistory = historyPairs
                )

                aiRepo.solveQuestionStream(request).collect { event ->
                    when (event) {
                        is AiStreamEvent.Analyzing -> {
                            _uiState.update { it.copy(status = AiUiStatus.Loading) }
                        }
                        is AiStreamEvent.Streaming -> {
                            _uiState.update { it.copy(status = AiUiStatus.Streaming(event.partialText)) }
                        }
                        is AiStreamEvent.Completed -> {
                            repository.appendAiMessage(convId, "assistant", event.fullText)
                            _uiState.update {
                                it.copy(
                                    status = AiUiStatus.Ready,
                                    lastAnswerText = event.fullText,
                                    lastOriginalQuestion = event.detectedQuestionText ?: it.lastOriginalQuestion,
                                    attachedImages = if (!isFollowUp) emptyList() else it.attachedImages
                                )
                            }
                        }
                        is AiStreamEvent.Error -> {
                            _uiState.update {
                                it.copy(status = AiUiStatus.Error(event.message, event.isUnreadableImage))
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(status = AiUiStatus.Error(e.localizedMessage ?: "AI সমাধানে ত্রুটি ঘটেছে।"))
                }
            }
        }
    }

    fun regenerateLastAnswer() {
        val state = _uiState.value
        val q = state.lastOriginalQuestion.ifBlank { state.questionInput }
        if (q.isBlank()) return
        _uiState.update { it.copy(questionInput = q) }
        submitQuestion(isFollowUp = false)
    }

    fun applyQuickTool(tool: AiQuickTool) {
        val state = _uiState.value
        val lastAnswer = activeMessages.value.lastOrNull { it.role == "assistant" }?.contentText
            ?: state.lastAnswerText
        val originalQ = state.lastOriginalQuestion.ifBlank { state.questionInput }
        val convId = state.activeConversationId ?: return
        if (lastAnswer.isBlank()) return

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            try {
                repository.appendAiMessage(convId, "user", "টুল প্রয়োগ: ${tool.labelBn}")
                _uiState.update { it.copy(status = AiUiStatus.Loading) }
                aiRepo.applyQuickToolStream(
                    previousAnswer = lastAnswer,
                    originalQuestion = originalQ,
                    tool = tool,
                    academicYear = state.selectedYear,
                    paperTitle = state.selectedPaperTitle
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Analyzing -> _uiState.update { it.copy(status = AiUiStatus.Loading) }
                        is AiStreamEvent.Streaming -> _uiState.update { it.copy(status = AiUiStatus.Streaming(event.partialText)) }
                        is AiStreamEvent.Completed -> {
                            repository.appendAiMessage(convId, "assistant", event.fullText)
                            _uiState.update { it.copy(status = AiUiStatus.Ready, lastAnswerText = event.fullText) }
                        }
                        is AiStreamEvent.Error -> _uiState.update { it.copy(status = AiUiStatus.Error(event.message)) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(status = AiUiStatus.Error(e.localizedMessage ?: "AI টুল পরিচালনা ব্যর্থ।")) }
            }
        }
    }
}

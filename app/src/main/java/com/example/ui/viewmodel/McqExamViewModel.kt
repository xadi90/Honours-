package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ExamResultEntity
import com.example.data.local.McqEntity
import com.example.data.repository.HonoursRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class McqSessionState(
    val isActive: Boolean = false,
    val isFinished: Boolean = false,
    val isTimedMode: Boolean = true,
    val examTitleBn: String = "মডেল টেস্ট",
    val paperId: Long = 0L,
    val questions: List<McqEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Long, Int> = emptyMap(),
    val instantFeedbackShown: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalAllowedSeconds: Int = 0,
    val lastSavedResult: ExamResultEntity? = null
)

class McqExamViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HonoursRepository(application.applicationContext)

    val allMcqs: StateFlow<List<McqEntity>> = repository.observeAllMcqs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val examResults: StateFlow<List<ExamResultEntity>> = repository.observeExamResults()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _session = MutableStateFlow(McqSessionState())
    val session: StateFlow<McqSessionState> = _session.asStateFlow()

    private var timerJob: Job? = null

    fun startExam(
        titleBn: String,
        paperId: Long?,
        chapterId: Long?,
        timedMode: Boolean
    ) {
        viewModelScope.launch {
            val pool = when {
                chapterId != null -> repository.observeMcqsByChapter(chapterId).first()
                paperId != null -> repository.observeMcqsByPaper(paperId).first()
                else -> repository.observeAllMcqs().first()
            }.shuffled()

            if (pool.isEmpty()) return@launch
            val duration = pool.size * 45 // 45 seconds per MCQ

            _session.value = McqSessionState(
                isActive = true,
                isFinished = false,
                isTimedMode = timedMode,
                examTitleBn = titleBn,
                paperId = paperId ?: 0L,
                questions = pool,
                currentIndex = 0,
                selectedAnswers = emptyMap(),
                instantFeedbackShown = false,
                remainingSeconds = duration,
                totalAllowedSeconds = duration
            )

            timerJob?.cancel()
            if (timedMode) {
                timerJob = viewModelScope.launch {
                    while (_session.value.isActive && !_session.value.isFinished && _session.value.remainingSeconds > 0) {
                        delay(1000)
                        _session.update { it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0)) }
                        if (_session.value.remainingSeconds == 0) {
                            finishExam()
                        }
                    }
                }
            }
        }
    }

    fun selectOption(questionId: Long, optionIndex: Int) {
        _session.update { state ->
            if (state.isFinished) return@update state
            val updated = state.selectedAnswers.toMutableMap()
            updated[questionId] = optionIndex
            state.copy(
                selectedAnswers = updated,
                instantFeedbackShown = !state.isTimedMode
            )
        }
    }

    fun nextQuestion() {
        _session.update { state ->
            if (state.currentIndex < state.questions.lastIndex) {
                state.copy(currentIndex = state.currentIndex + 1, instantFeedbackShown = false)
            } else state
        }
    }

    fun previousQuestion() {
        _session.update { state ->
            if (state.currentIndex > 0) {
                state.copy(currentIndex = state.currentIndex - 1, instantFeedbackShown = false)
            } else state
        }
    }

    fun finishExam() {
        timerJob?.cancel()
        val state = _session.value
        if (state.isFinished || state.questions.isEmpty()) return

        var correct = 0
        var wrong = 0
        state.questions.forEach { q ->
            val picked = state.selectedAnswers[q.id]
            if (picked != null) {
                if (picked == q.correctIndex) correct++ else wrong++
            }
        }
        val elapsed = (state.totalAllowedSeconds - state.remainingSeconds).coerceAtLeast(1).toLong()
        val result = ExamResultEntity(
            paperId = state.paperId,
            paperTitleBn = state.examTitleBn,
            totalQuestions = state.questions.size,
            correctCount = correct,
            wrongCount = wrong,
            timeTakenSeconds = if (state.isTimedMode) elapsed else (state.questions.size * 15L)
        )

        _session.update { it.copy(isFinished = true, lastSavedResult = result) }
        viewModelScope.launch {
            repository.saveExamResult(result)
        }
    }

    fun resetExam() {
        timerJob?.cancel()
        _session.value = McqSessionState()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

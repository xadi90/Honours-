package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ExamResultEntity
import com.example.data.local.PaperEntity
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldSoft
import com.example.ui.viewmodel.McqExamViewModel
import com.example.ui.viewmodel.McqSessionState
import java.util.Locale

@Composable
fun McqExamScreen(
    viewModel: McqExamViewModel,
    session: McqSessionState,
    papers: List<PaperEntity>,
    examResults: List<ExamResultEntity>,
    totalMcqCount: Int,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = if (session.isActive) session.examTitleBn else "MCQ ও পরীক্ষা ব্যবস্থা",
            subtitle = if (session.isActive && session.isTimedMode && !session.isFinished) {
                val mins = session.remainingSeconds / 60
                val secs = session.remainingSeconds % 60
                String.format(Locale.US, "অবশিষ্ট সময়: %02d:%02d", mins, secs)
            } else "অধ্যায়, পত্র ও মডেল টেস্ট প্রস্তুতি",
            onBack = {
                if (session.isActive) viewModel.resetExam() else onBack()
            }
        )

        if (!session.isActive) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("সমন্বিত মডেল টেস্ট (${totalMcqCount}টি প্রশ্ন)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("জাতীয় বিশ্ববিদ্যালয়ের রাষ্ট্রবিজ্ঞান অনার্সের গুরুত্বপূর্ণ প্রশ্ন নিয়ে সময়সীমাভিত্তিক পরীক্ষা অথবা তাৎক্ষণিক ব্যাখ্যাসহ অনুশীলন।", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.startExam("পূর্ণাঙ্গ মডেল টেস্ট (সময়সীমাবদ্ধ)", null, null, timedMode = true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mcq_start_timed_model"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Timer, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("টাইমড পরীক্ষা")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.startExam("তাৎক্ষণিক অনুশীলন মোড", null, null, timedMode = false) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mcq_start_practice_model"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Quiz, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("অনুশীলন")
                                }
                            }
                        }
                    }
                }

                item {
                    Text("পত্রভিত্তিক MCQ পরীক্ষা", style = MaterialTheme.typography.titleLarge)
                }

                items(papers.filter { it.code in setOf("211901", "211903", "211907", "211501", "221901", "221907", "231901", "231903", "241901", "241911", "241917") }, key = { it.id }) { paper ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${paper.code} • ${paper.titleBn}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(paper.titleEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.startExam("${paper.code} অনুশীলন", paper.id, null, timedMode = false) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("শুরু")
                            }
                        }
                    }
                }

                if (examResults.isNotEmpty()) {
                    item {
                        Text("পূর্ববর্তী পরীক্ষার ফলাফল (${examResults.size})", style = MaterialTheme.typography.titleLarge)
                    }
                    items(examResults, key = { it.id }) { res ->
                        val pct = if (res.totalQuestions > 0) (res.correctCount * 100) / res.totalQuestions else 0
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(res.paperTitleBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("সঠিক: ${res.correctCount} | ভুল: ${res.wrongCount} | সময়: ${res.timeTakenSeconds} সে.", style = MaterialTheme.typography.bodySmall)
                                }
                                Surface(color = GoldSoft, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (session.isFinished) {
            val res = session.lastSavedResult
            val total = res?.totalQuestions ?: session.questions.size
            val correct = res?.correctCount ?: 0
            val wrong = res?.wrongCount ?: 0
            val pct = if (total > 0) (correct * 100) / total else 0
            val analysis = when {
                pct >= 80 -> "অসাধারণ প্রস্তুতি! আপনি জাতীয় বিশ্ববিদ্যালয়ের পরীক্ষায় সর্বোচ্চ ফলাফলের জন্য প্রস্তুত।"
                pct >= 60 -> "ভালো প্রস্তুতি! আরও কয়েকটি মডেল টেস্ট ও অধ্যায় রিভিশনে দক্ষতা আরও বাড়বে।"
                else -> "আরও অনুশীলন প্রয়োজন। ভুল উত্তরগুলোর ব্যাখ্যা ও অধ্যায়ের সংজ্ঞা ভালোভাবে পড়ুন।"
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("পরীক্ষার ফলাফল: $pct%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("মোট প্রশ্ন: $total • সঠিক: $correct • ভুল: $wrong • সময়: ${res?.timeTakenSeconds ?: 0} সেকেন্ড", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(10.dp))
                            Text(analysis, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(14.dp))
                            Button(onClick = { viewModel.resetExam() }, modifier = Modifier.testTag("mcq_back_to_menu")) {
                                Text("নতুন পরীক্ষা দিন")
                            }
                        }
                    }
                }

                item {
                    Text("উত্তর পর্যালোচনা ও ব্যাখ্যা", style = MaterialTheme.typography.titleLarge)
                }

                items(session.questions, key = { it.id }) { q ->
                    val picked = session.selectedAnswers[q.id]
                    val isCorrect = picked == q.correctIndex
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isCorrect) EmeraldSuccess else CrimsonError)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(q.questionBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            val opts = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                            Text("সঠিক উত্তর: ${opts[q.correctIndex]}", color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
                            if (picked != null && !isCorrect) {
                                Text("আপনার উত্তর: ${opts[picked]}", color = CrimsonError)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("ব্যাখ্যা: ${q.explanationBn}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        } else {
            val q = session.questions.getOrNull(session.currentIndex) ?: return@Column
            val selected = session.selectedAnswers[q.id]
            val opts = listOf(q.optionA, q.optionB, q.optionC, q.optionD)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LinearProgressIndicator(
                        progress = { (session.currentIndex + 1).toFloat() / session.questions.size.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "প্রশ্ন ${session.currentIndex + 1} / ${session.questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = q.questionBn,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    opts.forEachIndexed { idx, optText ->
                        val isSelected = selected == idx
                        val showFeedback = session.instantFeedbackShown && selected != null
                        val bgColor = when {
                            showFeedback && idx == q.correctIndex -> EmeraldSoft
                            showFeedback && isSelected && idx != q.correctIndex -> CrimsonSoft
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Card(
                            onClick = { viewModel.selectOption(q.id, idx) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mcq_option_$idx"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = listOf("(ক)", "(খ)", "(গ)", "(ঘ)")[idx],
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(text = optText, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                if (showFeedback && idx == q.correctIndex) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "সঠিক", tint = EmeraldSuccess)
                                }
                            }
                        }
                    }

                    if (session.instantFeedbackShown && selected != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ব্যাখ্যা: ${q.explanationBn}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.previousQuestion() },
                        enabled = session.currentIndex > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("পূর্ববর্তী")
                    }
                    if (session.currentIndex < session.questions.lastIndex) {
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("পরবর্তী")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.finishExam() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mcq_submit_button")
                        ) {
                            Text("ফলাফল দেখুন")
                        }
                    }
                }
            }
        }
    }
}

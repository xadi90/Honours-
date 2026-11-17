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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ChapterEntity
import com.example.data.local.ExamResultEntity
import com.example.data.local.StudyGoalEntity
import com.example.data.prefs.StudyPlannerPrefs
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import java.util.concurrent.TimeUnit

@Composable
fun StudyPlannerScreen(
    goals: List<StudyGoalEntity>,
    chapters: List<ChapterEntity>,
    examResults: List<ExamResultEntity>,
    plannerPrefs: StudyPlannerPrefs,
    onAddGoal: (String, Int, String) -> Unit,
    onToggleGoal: (StudyGoalEntity) -> Unit,
    onDeleteGoal: (StudyGoalEntity) -> Unit,
    onUpdatePlannerPrefs: (Int, String, Long) -> Unit,
    onBack: () -> Unit
) {
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showCountdownDialog by remember { mutableStateOf(false) }

    val completedChapters = chapters.count { it.isCompleted }
    val chapterProgress = if (chapters.isNotEmpty()) completedChapters.toFloat() / chapters.size.toFloat() else 0f
    val totalGoalMinutes = goals.sumOf { it.targetMinutes }
    val completedMinutes = goals.sumOf { it.completedMinutes }
    val daysLeft = TimeUnit.MILLISECONDS.toDays((plannerPrefs.examTargetDateMillis - System.currentTimeMillis()).coerceAtLeast(0L))

    Scaffold(
        topBar = {
            HonoursTopBar(
                title = "ব্যক্তিগত স্টাডি প্ল্যানার",
                subtitle = "দৈনিক লক্ষ্য, অগ্রগতি ও পরীক্ষা কাউন্টডাউন",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                modifier = Modifier.testTag("planner_add_goal_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "নতুন লক্ষ্য যোগ করুন")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Exam Countdown Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Event, contentDescription = null, tint = GoldAccent)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(plannerPrefs.examCountdownTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("বাকি আছে: $daysLeft দিন • দৈনিক লক্ষ্য: ${plannerPrefs.dailyGoalMinutes} মিনিট", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { showCountdownDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "সম্পাদনা")
                        }
                    }
                }
            }

            // Syllabus Chapter Progress Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "অধ্যায় সমাপ্তির অগ্রগতি: ${(chapterProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(progress = { chapterProgress }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Text("$completedChapters / ${chapters.size} অধ্যায় সম্পন্ন • আজকের পড়া: $completedMinutes / ${plannerPrefs.dailyGoalMinutes} মিনিট (প্ল্যানকৃত: $totalGoalMinutes মিনিট)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Text("পড়াশোনার লক্ষ্য ও সাপ্তাহিক পরিকল্পনা", style = MaterialTheme.typography.titleLarge)
            }

            items(goals, key = { it.id }) { goal ->
                Card(
                    onClick = { onToggleGoal(goal) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_goal_${goal.id}"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (goal.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = "সম্পন্ন",
                            tint = if (goal.isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(goal.titleBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${goal.dayLabelBn} • ${goal.targetMinutes} মিনিট", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDeleteGoal(goal) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "মুছুন")
                        }
                    }
                }
            }

            if (examResults.isNotEmpty()) {
                item {
                    Text("সাম্প্রতিক পরীক্ষা ইতিহাস", style = MaterialTheme.typography.titleLarge)
                }
                items(examResults.take(4), key = { "hist_${it.id}" }) { r ->
                    val pct = if (r.totalQuestions > 0) (r.correctCount * 100) / r.totalQuestions else 0
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(r.paperTitleBn, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("$pct% (${r.correctCount}/${r.totalQuestions})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        var title by remember { mutableStateOf("") }
        var minutesText by remember { mutableStateOf("45") }
        var dayLabel by remember { mutableStateOf("আজ") }
        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("নতুন স্টাডি লক্ষ্য যুক্ত করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("লক্ষ্যের বিবরণ") })
                    OutlinedTextField(value = minutesText, onValueChange = { minutesText = it }, label = { Text("সময় (মিনিট)") })
                    OutlinedTextField(value = dayLabel, onValueChange = { dayLabel = it }, label = { Text("বার / সময় (যেমন: আজ, শুক্রবার)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddGoal(title, minutesText.toIntOrNull() ?: 45, dayLabel.ifBlank { "আজ" })
                    showAddGoalDialog = false
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) { Text("বাতিল") }
            }
        )
    }

    if (showCountdownDialog) {
        var examTitle by remember { mutableStateOf(plannerPrefs.examCountdownTitle) }
        var dailyMins by remember { mutableStateOf(plannerPrefs.dailyGoalMinutes.toString()) }
        var daysStr by remember { mutableStateOf(daysLeft.toString()) }
        AlertDialog(
            onDismissRequest = { showCountdownDialog = false },
            title = { Text("পরীক্ষার কাউন্টডাউন সেটিংস") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = examTitle, onValueChange = { examTitle = it }, label = { Text("পরীক্ষার নাম") })
                    OutlinedTextField(value = daysStr, onValueChange = { daysStr = it }, label = { Text("কত দিন বাকি?") })
                    OutlinedTextField(value = dailyMins, onValueChange = { dailyMins = it }, label = { Text("দৈনিক পড়ার লক্ষ্য (মিনিট)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val d = (daysStr.toLongOrNull() ?: 30L).coerceIn(1L, 365L)
                    val targetMillis = System.currentTimeMillis() + d * 24L * 60L * 60L * 1000L
                    onUpdatePlannerPrefs(dailyMins.toIntOrNull() ?: 90, examTitle, targetMillis)
                    showCountdownDialog = false
                }) { Text("আপডেট") }
            },
            dismissButton = {
                TextButton(onClick = { showCountdownDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

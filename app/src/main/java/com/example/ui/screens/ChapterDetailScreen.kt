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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ChapterEntity
import com.example.data.local.McqEntity
import com.example.data.local.PaperEntity
import com.example.data.local.QuestionAnswerEntity
import com.example.data.model.QuestionCategory
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.QuestionAnswerCard

@Composable
fun ChapterDetailScreen(
    paper: PaperEntity?,
    chapter: ChapterEntity?,
    questions: List<QuestionAnswerEntity>,
    mcqs: List<McqEntity>,
    onBack: () -> Unit,
    onToggleBookmark: (Long, Boolean) -> Unit,
    onToggleSaveOffline: (Long, Boolean) -> Unit,
    onStartChapterMcq: (ChapterEntity) -> Unit,
    onAskAi: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = chapter?.let { "অধ্যায় ${it.chapterNumber}: ${it.titleBn}" } ?: "অধ্যায়ের সমাধান",
            subtitle = paper?.let { "${it.code} • ${it.titleBn}" },
            onBack = onBack
        )

        if (chapter == null) {
            EmptyStateView(
                title = "অধ্যায়টি পাওয়া যায়নি",
                message = "অনুগ্রহ করে পূর্ববর্তী পর্দায় ফিরে যান।",
                icon = Icons.Filled.MenuBook,
                actionLabel = "ফিরে যান",
                onAction = onBack
            )
            return@Column
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("পরিচিতি ও সংজ্ঞা") },
                modifier = Modifier.testTag("chapter_tab_overview")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("প্রশ্নোত্তর (${questions.size})") },
                modifier = Modifier.testTag("chapter_tab_qa")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("MCQ (${mcqs.size})") },
                modifier = Modifier.testTag("chapter_tab_mcq")
            )
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SectionInfoCard(
                            title = "অধ্যায় পরিচিতি (Introduction)",
                            body = chapter.introductionBn
                        )
                    }
                    item {
                        SectionInfoCard(
                            title = "গুরুত্বপূর্ণ সংজ্ঞা (Key Definitions)",
                            body = chapter.keyDefinitionsBn
                        )
                    }
                    item {
                        SectionInfoCard(
                            title = "পরীক্ষার গুরুত্বপূর্ণ টপিকসমূহ",
                            body = chapter.importantTopicsBn
                        )
                    }
                    item {
                        Button(
                            onClick = onAskAi,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chapter_ask_ai_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("এই অধ্যায়ের কোনো প্রশ্ন AI-কে জিজ্ঞাসা করুন")
                        }
                    }
                }
            }

            1 -> {
                val filteredQuestions = if (selectedCategory == null) {
                    questions
                } else {
                    questions.filter { it.category == selectedCategory }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = { Text("সব (${questions.size})") }
                                )
                            }
                            items(QuestionCategory.entries) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat.name,
                                    onClick = { selectedCategory = cat.name },
                                    label = { Text(cat.labelBn) }
                                )
                            }
                        }
                    }

                    if (filteredQuestions.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "এই বিভাগে এখনো প্রশ্নোত্তর নেই",
                                message = "আপনি AI Question Solver-এ প্রশ্নটি লিখে বা ছবি তুলে তাৎক্ষণিক সমাধান পেতে পারেন।",
                                icon = Icons.Filled.AutoAwesome,
                                actionLabel = "AI-তে সমাধান খুঁজুন",
                                onAction = onAskAi
                            )
                        }
                    } else {
                        items(filteredQuestions, key = { it.id }) { qa ->
                            QuestionAnswerCard(
                                qa = qa,
                                onToggleBookmark = { onToggleBookmark(qa.id, it) },
                                onToggleSaveOffline = { onToggleSaveOffline(qa.id, it) }
                            )
                        }
                    }
                }
            }

            2 -> {
                if (mcqs.isEmpty()) {
                    EmptyStateView(
                        title = "এই অধ্যায়ে এখনো MCQ যুক্ত করা হয়নি",
                        message = "কোর্সভিত্তিক বা পূর্ণাঙ্গ মডেল টেস্টে অংশগ্রহণ করুন।",
                        icon = Icons.Filled.Quiz
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Button(
                                onClick = { onStartChapterMcq(chapter) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chapter_start_mcq_quiz"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Quiz, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("অধ্যায়ভিত্তিক MCQ পরীক্ষা শুরু করুন (${mcqs.size}টি)")
                            }
                        }
                        items(mcqs, key = { it.id }) { mcq ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = mcq.questionBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    val opts = listOf(mcq.optionA, mcq.optionB, mcq.optionC, mcq.optionD)
                                    opts.forEachIndexed { idx, opt ->
                                        val prefix = listOf("ক", "খ", "গ", "ঘ")[idx]
                                        val isCorrect = idx == mcq.correctIndex
                                        Text(
                                            text = "($prefix) $opt ${if (isCorrect) "✓" else ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "ব্যাখ্যা: ${mcq.explanationBn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionInfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body.ifBlank { "তথ্য হালনাগাদ করা হচ্ছে।" },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ChapterEntity
import com.example.data.local.PaperEntity
import com.example.data.model.AcademicYearLevel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.EmeraldSuccess

@Composable
fun PaperDetailScreen(
    paper: PaperEntity?,
    chapters: List<ChapterEntity>,
    questionCount: Int,
    onBack: () -> Unit,
    onChapterClick: (Long) -> Unit,
    onToggleChapterComplete: (Long, Boolean) -> Unit,
    onStartPaperMcq: (PaperEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = paper?.titleBn ?: "কোর্সের বিবরণ",
            subtitle = paper?.let { "${AcademicYearLevel.fromYear(it.yearNumber).titleBn} • কোড: ${it.code}" },
            onBack = onBack
        )

        if (paper == null) {
            EmptyStateView(
                title = "কোর্সটি খুঁজে পাওয়া যায়নি",
                message = "অনুগ্রহ করে পুনরায় বিষয় তালিকা থেকে নির্বাচন করুন।",
                icon = Icons.Filled.AutoStories,
                actionLabel = "ফিরে যান",
                onAction = onBack
            )
            return@Column
        }

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
                        Text(
                            text = paper.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "পত্র কোড: ${paper.code} • ${if (paper.credits > 0) "${paper.credits} ক্রেডিট" else "নন-ক্রেডিট"} • ${paper.marks} নম্বর • সংযুক্ত প্রশ্ন: $questionCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onStartPaperMcq(paper) },
                            modifier = Modifier.testTag("paper_start_mcq_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Quiz, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("এই পত্রের MCQ অনুশীলন")
                        }
                    }
                }
            }

            if (chapters.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "অধ্যায়ভিত্তিক নোট শীঘ্রই আসছে",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "এই পত্রের যাচাইকৃত অধ্যায় এখনও যুক্ত করা হয়নি। অনুমোদিত অ্যাডমিন প্যানেল থেকে নতুন অধ্যায় ও প্রশ্নোত্তর যুক্ত করা যাবে অথবা আপনি AI Question Solver থেকে যেকোনো প্রশ্নের সমাধান পেতে পারেন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "অধ্যায় তালিকা (${chapters.size}টি)",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                items(chapters, key = { it.id }) { chapter ->
                    Card(
                        onClick = { onChapterClick(chapter.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chapter_card_${chapter.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onToggleChapterComplete(chapter.id, !chapter.isCompleted) },
                                modifier = Modifier.testTag("chapter_complete_toggle_${chapter.id}")
                            ) {
                                Icon(
                                    imageVector = if (chapter.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = "অধ্যায় সম্পন্ন হিসেবে চিহ্নিত করুন",
                                    tint = if (chapter.isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "অধ্যায় ${chapter.chapterNumber}: ${chapter.titleBn}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (chapter.titleEn.isNotBlank()) {
                                    Text(
                                        text = chapter.titleEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = chapter.importantTopicsBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "অধ্যায় খুলুন"
                            )
                        }
                    }
                }
            }
        }
    }
}

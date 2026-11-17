package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SearchHistoryEntity
import com.example.data.repository.SearchResultBundle
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.QuestionAnswerCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    query: String,
    isSearching: Boolean,
    results: SearchResultBundle,
    history: List<SearchHistoryEntity>,
    onQueryChange: (String) -> Unit,
    onClearHistory: () -> Unit,
    onPaperClick: (Long) -> Unit,
    onChapterClick: (Long) -> Unit,
    onToggleBookmark: (Long, Boolean) -> Unit,
    onToggleSaveOffline: (Long, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val popularTerms = listOf(
        "সার্বভৌমত্ব", "211901", "১৯৭২ সালের সংবিধান", "প্লেটো", "মর্গেনথাউ", "গ্রামসি", "১৯৩৫", "Public Policy"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "স্মার্ট একাডেমিক অনুসন্ধান",
            subtitle = "বাংলা, ইংরেজি, পত্র কোড বা অধ্যায় দিয়ে খুঁজুন",
            onBack = onBack
        )

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("যেমন: রাষ্ট্র, 211901, Sovereignty, গ্রামসি...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.testTag("search_clear_button")
                    ) {
                        Icon(Icons.Filled.Clear, contentDescription = "পরিষ্কার করুন")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_input_field")
        )

        if (isSearching) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (query.isBlank()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("জনপ্রিয় অনুসন্ধান", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularTerms.forEach { term ->
                            AssistChip(
                                onClick = { onQueryChange(term) },
                                label = { Text(term) }
                            )
                        }
                    }
                }

                if (history.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("সাম্প্রতিক অনুসন্ধান", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(
                                onClick = onClearHistory,
                                modifier = Modifier.testTag("search_clear_history")
                            ) {
                                Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                                Text("মুছুন")
                            }
                        }
                        history.forEach { item ->
                            Card(
                                onClick = { onQueryChange(item.query) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.History, contentDescription = null)
                                    Spacer(Modifier.padding(6.dp))
                                    Text(item.query, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        } else if (results.isEmpty && !isSearching) {
            EmptyStateView(
                title = "'$query'-এর জন্য কোনো ফলাফল পাওয়া যায়নি",
                message = "বানান যাচাই করুন অথবা পত্র কোড (যেমন 211901, 231901) দিয়ে অনুসন্ধান করুন।",
                icon = Icons.Filled.SearchOff,
                actionLabel = "অনুসন্ধান মুছুন",
                onAction = { onQueryChange("") }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (results.papers.isNotEmpty()) {
                    item {
                        Text("কোর্স / পত্র (${results.papers.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(results.papers, key = { "p_${it.id}" }) { paper ->
                        Card(
                            onClick = { onPaperClick(paper.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("${paper.code} • ${paper.titleBn}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(paper.titleEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                if (results.chapters.isNotEmpty()) {
                    item {
                        Text("অধ্যায়সমূহ (${results.chapters.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(results.chapters, key = { "c_${it.id}" }) { chapter ->
                        Card(
                            onClick = { onChapterClick(chapter.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("অধ্যায় ${chapter.chapterNumber}: ${chapter.titleBn}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(chapter.importantTopicsBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                if (results.questions.isNotEmpty()) {
                    item {
                        Text("প্রশ্ন ও উত্তর (${results.questions.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(results.questions, key = { "q_${it.id}" }) { qa ->
                        QuestionAnswerCard(
                            qa = qa,
                            onToggleBookmark = { onToggleBookmark(qa.id, it) },
                            onToggleSaveOffline = { onToggleSaveOffline(qa.id, it) }
                        )
                    }
                }
            }
        }
    }
}

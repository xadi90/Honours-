package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.PaperEntity
import com.example.data.local.QuestionAnswerEntity
import com.example.data.model.AcademicYearLevel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.QuestionAnswerCard

@Composable
fun QuestionBankScreen(
    questions: List<QuestionAnswerEntity>,
    papers: List<PaperEntity>,
    onBack: () -> Unit,
    onToggleBookmark: (Long, Boolean) -> Unit,
    onToggleSaveOffline: (Long, Boolean) -> Unit
) {
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }
    var onlyPastExam by rememberSaveable { mutableStateOf(true) }
    var searchText by rememberSaveable { mutableStateOf("") }

    val paperById = remember(papers) { papers.associateBy { it.id } }

    val filtered = remember(questions, papers, selectedYear, onlyPastExam, searchText) {
        questions.filter { qa ->
            val paper = paperById[qa.paperId]
            val yearMatch = selectedYear == null || paper?.yearNumber == selectedYear
            val examMatch = !onlyPastExam || qa.isPastExamQuestion
            val textMatch = searchText.isBlank() ||
                qa.questionBn.contains(searchText, ignoreCase = true) ||
                qa.questionEn.contains(searchText, ignoreCase = true) ||
                (paper?.code?.contains(searchText, ignoreCase = true) == true)
            yearMatch && examMatch && textMatch
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "বিগত সালের প্রশ্নব্যাংক",
            subtitle = "প্রকৃত বিগত প্রশ্ন ও মডেল অনুশীলন পৃথকভাবে চিহ্নিত",
            onBack = onBack
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("প্রশ্ন বা পত্র কোড লিখুন (যেমন: 211901, সংবিধান)...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("qbank_search_input")
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = onlyPastExam,
                    onClick = { onlyPastExam = true },
                    label = { Text("বিগত পরীক্ষার প্রশ্ন") }
                )
            }
            item {
                FilterChip(
                    selected = !onlyPastExam,
                    onClick = { onlyPastExam = false },
                    label = { Text("সকল প্রশ্ন (মডেলসহ)") }
                )
            }
            items(AcademicYearLevel.entries) { yr ->
                FilterChip(
                    selected = selectedYear == yr.yearNumber,
                    onClick = { selectedYear = if (selectedYear == yr.yearNumber) null else yr.yearNumber },
                    label = { Text(yr.shortBn) }
                )
            }
        }

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "কোনো প্রশ্ন পাওয়া যায়নি",
                message = "ফিল্টার পরিবর্তন করে আবার চেষ্টা করুন অথবা 'সকল প্রশ্ন' নির্বাচন করুন।",
                icon = Icons.Filled.HistoryEdu,
                actionLabel = "সব প্রশ্ন দেখান",
                onAction = {
                    selectedYear = null
                    onlyPastExam = false
                    searchText = ""
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { qa ->
                    val paper = paperById[qa.paperId]
                    val label = paper?.let { "${it.code} • ${it.titleBn}" } ?: ""
                    QuestionAnswerCard(
                        qa = qa,
                        paperInfoLabel = label,
                        onToggleBookmark = { onToggleBookmark(qa.id, it) },
                        onToggleSaveOffline = { onToggleSaveOffline(qa.id, it) }
                    )
                }
            }
        }
    }
}

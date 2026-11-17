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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ChapterEntity
import com.example.data.local.PaperEntity
import com.example.data.model.AcademicYearLevel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.GoldSoft

@Composable
fun SubjectsScreen(
    initialYear: Int,
    papers: List<PaperEntity>,
    chapters: List<ChapterEntity>,
    onPaperClick: (Long) -> Unit,
    onOpenSearch: () -> Unit
) {
    var selectedYearIndex by rememberSaveable(initialYear) {
        mutableIntStateOf((initialYear - 1).coerceIn(0, 3))
    }
    val years = AcademicYearLevel.entries
    val activeYear = years[selectedYearIndex]
    val filteredPapers = papers.filter { it.yearNumber == activeYear.yearNumber }

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "পাঠ্যক্রম ও বিষয়সমূহ",
            subtitle = "জাতীয় বিশ্ববিদ্যালয় • রাষ্ট্রবিজ্ঞান বিভাগ",
            actions = {
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.testTag("subjects_search_button")
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "অনুসন্ধান")
                }
            }
        )

        ScrollableTabRow(
            selectedTabIndex = selectedYearIndex,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            years.forEachIndexed { index, year ->
                Tab(
                    selected = selectedYearIndex == index,
                    onClick = { selectedYearIndex = index },
                    text = {
                        Text(
                            text = year.shortBn,
                            fontWeight = if (selectedYearIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("subjects_year_tab_${year.yearNumber}")
                )
            }
        }

        if (filteredPapers.isEmpty()) {
            EmptyStateView(
                title = "${activeYear.titleBn}-এর কোনো পত্র পাওয়া যায়নি",
                message = "অনুমোদিত অ্যাডমিন প্যানেল থেকে নতুন কোর্স যুক্ত করুন।",
                icon = Icons.Filled.MenuBook
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPapers, key = { it.id }) { paper ->
                    val paperChapters = chapters.filter { it.paperId == paper.id }
                    val completedCount = paperChapters.count { it.isCompleted }

                    Card(
                        onClick = { onPaperClick(paper.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paper_card_${paper.code}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "পত্র কোড: ${paper.code}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        color = GoldSoft,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (paper.credits > 0) "${paper.credits} ক্রেডিট • ${paper.marks} নম্বর" else "নন-ক্রেডিট • ${paper.marks} নম্বর",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = paper.titleBn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = paper.titleEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = if (paperChapters.isNotEmpty()) {
                                        "অধ্যায়: ${paperChapters.size}টি (সম্পন্ন: ${completedCount}টি)"
                                    } else {
                                        "সিলেবাস অন্তর্ভুক্ত • অধ্যায় যুক্ত করার জন্য প্রস্তুত"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "পত্র খুলুন",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

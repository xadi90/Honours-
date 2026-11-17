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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SuggestionEntity
import com.example.data.model.AcademicYearLevel
import com.example.data.model.DeveloperConstants
import com.example.data.model.SuggestionCategory
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.GoldSoft

@Composable
fun SuggestionsScreen(
    suggestions: List<SuggestionEntity>,
    onBack: () -> Unit
) {
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedCat by rememberSaveable { mutableStateOf<String?>(null) }

    val filtered = remember(suggestions, selectedYear, selectedCat) {
        suggestions.filter { s ->
            (selectedYear == null || s.yearNumber == selectedYear) &&
                (selectedCat == null || s.category == selectedCat)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "পরীক্ষার সাজেশন",
            subtitle = "জাতীয় বিশ্ববিদ্যালয় রাষ্ট্রবিজ্ঞান অনার্স",
            onBack = onBack
        )

        // Mandatory Disclaimer
        Surface(
            color = AmberSoft,
            contentColor = Color(0xFF78350F),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.WarningAmber, contentDescription = "সতর্কীকরণ")
                Spacer(Modifier.width(8.dp))
                Text(
                    text = DeveloperConstants.SUGGESTION_DISCLAIMER_BN,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedYear == null,
                    onClick = { selectedYear = null },
                    label = { Text("সকল বর্ষ") }
                )
            }
            items(AcademicYearLevel.entries) { yr ->
                FilterChip(
                    selected = selectedYear == yr.yearNumber,
                    onClick = { selectedYear = yr.yearNumber },
                    label = { Text(yr.shortBn) }
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCat == null,
                    onClick = { selectedCat = null },
                    label = { Text("সব ক্যাটাগরি") }
                )
            }
            items(SuggestionCategory.entries) { cat ->
                FilterChip(
                    selected = selectedCat == cat.name,
                    onClick = { selectedCat = cat.name },
                    label = { Text(cat.labelBn) }
                )
            }
        }

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "এই ফিল্টারে কোনো সাজেশন নেই",
                message = "অনুমোদিত অ্যাডমিন প্যানেল থেকে নতুন সাজেশন প্রকাশ করা হলে এখানে দেখা যাবে।",
                icon = Icons.Filled.Lightbulb
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { s ->
                    val cat = SuggestionCategory.fromName(s.category)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        text = "${AcademicYearLevel.fromYear(s.yearNumber).shortBn} • ${s.paperCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(color = GoldSoft, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        text = cat.labelBn,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(s.paperTitleBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.height(10.dp))
                            Text(s.itemsTextBn, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

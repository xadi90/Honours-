package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueSoft

@Composable
fun StudyHubScreen(
    pastQuestionCount: Int,
    suggestionCount: Int,
    materialCount: Int,
    mcqCount: Int,
    activeGoalCount: Int,
    onOpenQuestionBank: () -> Unit,
    onOpenSuggestions: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenMcqExam: () -> Unit,
    onOpenStudyPlanner: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "পড়াশোনা ও পরীক্ষা প্রস্তুতি",
            subtitle = "প্রশ্নব্যাংক, সাজেশন, PDF লাইব্রেরি, MCQ ও প্ল্যানার"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StudyModuleCard(
                    title = "বিগত সালের প্রশ্নব্যাংক (Question Bank)",
                    subtitle = "বর্ষ, পত্র ও অধ্যায়ভিত্তিক বিগত পরীক্ষার প্রশ্ন ও যাচাইকৃত সমাধান ($pastQuestionCount+ প্রশ্ন)",
                    icon = Icons.Filled.HistoryEdu,
                    accentColor = RoyalBlue,
                    badgeBg = RoyalBlueSoft,
                    onClick = onOpenQuestionBank,
                    tag = "study_hub_question_bank"
                )
            }
            item {
                StudyModuleCard(
                    title = "পরীক্ষার সাজেশন (Suggestions)",
                    subtitle = "অধ্যায়ভিত্তিক, সংক্ষিপ্ত, রচনামূলক ও চূড়ান্ত পরীক্ষার প্রস্তুতি সাজেশন ($suggestionCount সেট)",
                    icon = Icons.Filled.Lightbulb,
                    accentColor = GoldAccent,
                    badgeBg = GoldSoft,
                    onClick = onOpenSuggestions,
                    tag = "study_hub_suggestions"
                )
            }
            item {
                StudyModuleCard(
                    title = "PDF ও স্টাডি ম্যাটেরিয়াল লাইব্রেরি",
                    subtitle = "অফলাইন ডাউনলোড, জুম, বুকমার্ক ও পূর্ণাঙ্গ PDF রিডার (${materialCount}টি ডকুমেন্ট)",
                    icon = Icons.Filled.PictureAsPdf,
                    accentColor = Color(0xFFB91C1C),
                    badgeBg = Color(0xFFFEE2E2),
                    onClick = onOpenLibrary,
                    tag = "study_hub_library"
                )
            }
            item {
                StudyModuleCard(
                    title = "MCQ ও সময়সীমাভিত্তিক মডেল টেস্ট",
                    subtitle = "অধ্যায়ভিত্তিক অনুশীলন, তাৎক্ষণিক ব্যাখ্যা ও স্কোর বিশ্লেষণ (${mcqCount}টি MCQ)",
                    icon = Icons.Filled.Quiz,
                    accentColor = EmeraldSuccess,
                    badgeBg = EmeraldSoft,
                    onClick = onOpenMcqExam,
                    tag = "study_hub_mcq"
                )
            }
            item {
                StudyModuleCard(
                    title = "ব্যক্তিগত স্টাডি প্ল্যানার (Study Planner)",
                    subtitle = "দৈনিক লক্ষ্য, অধ্যায় সমাপ্তির অগ্রগতি ও পরীক্ষার কাউন্টডাউন (${activeGoalCount}টি লক্ষ্য)",
                    icon = Icons.Filled.EventNote,
                    accentColor = MaterialTheme.colorScheme.primary,
                    badgeBg = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onOpenStudyPlanner,
                    tag = "study_hub_planner"
                )
            }
        }
    }
}

@Composable
private fun StudyModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badgeBg: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        shape = RoundedCornerShape(18.dp),
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
            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(54.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.padding(13.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "খুলুন"
            )
        }
    }
}

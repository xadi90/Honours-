package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AnnouncementEntity
import com.example.data.local.PaperEntity
import com.example.data.local.QuestionAnswerEntity
import com.example.data.local.StudyMaterialEntity
import com.example.data.model.AcademicYearLevel
import com.example.data.model.DeveloperConstants
import com.example.data.prefs.StudentProfileState
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlue

@Composable
fun HomeScreen(
    profile: StudentProfileState,
    papers: List<PaperEntity>,
    recentMaterials: List<StudyMaterialEntity>,
    latestMaterials: List<StudyMaterialEntity>,
    bookmarkedQuestions: List<QuestionAnswerEntity>,
    announcements: List<AnnouncementEntity>,
    onOpenSearch: () -> Unit,
    onOpenAiSolver: () -> Unit,
    onOpenYear: (Int) -> Unit,
    onOpenQuestionBank: () -> Unit,
    onOpenSuggestions: () -> Unit,
    onOpenMcqExam: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenStudyPlanner: () -> Unit,
    onOpenMaterial: (StudyMaterialEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Academic Welcome Header + Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(NavyDeep, NavyPrimary)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "স্বাগতম, ${profile.studentName}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${DeveloperConstants.DEPARTMENT} • ${AcademicYearLevel.fromYear(profile.academicYear).titleBn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldBright
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalance,
                                contentDescription = "জাতীয় বিশ্ববিদ্যালয়",
                                tint = GoldBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "NU BD",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Prominent Search Bar Trigger
                Surface(
                    onClick = onOpenSearch,
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("home_search_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "অনুসন্ধান করুন",
                            tint = NavyPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "পত্র কোড, অধ্যায় বা প্রশ্ন খুঁজুন (বাংলা / English)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 2. Prominent AI Question Solver Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onOpenAiSolver)
                    .testTag("home_ai_solver_card"),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_ai_solver_banner_1794931531035),
                        contentDescription = "AI Question Solver Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        NavyDeep.copy(alpha = 0.94f),
                                        NavyPrimary.copy(alpha = 0.78f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = GoldBright,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = "AI টিউটর",
                                        tint = NavyDeep,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "AI ACADEMIC TUTOR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NavyDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Column {
                            Text(
                                text = "AI Question Solver",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "প্রশ্ন লিখে বা বই/প্রশ্নপত্রের ছবি তুলে সমাধান পান",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Button(
                            onClick = onOpenAiSolver,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldBright,
                                contentColor = NavyDeep
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("home_ai_solver_cta")
                        ) {
                            Text("প্রশ্ন জিজ্ঞাসা করুন", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Four Academic Year Cards
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "অনার্স বর্ষসমূহ (রাষ্ট্রবিজ্ঞান বিভাগ)",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(10.dp))

                val years = AcademicYearLevel.entries
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (rowIndex in 0..1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (colIndex in 0..1) {
                                val year = years[rowIndex * 2 + colIndex]
                                val count = papers.count { it.yearNumber == year.yearNumber }
                                AcademicYearCard(
                                    year = year,
                                    paperCount = count,
                                    onClick = { onOpenYear(year.yearNumber) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Access Feature Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "দ্রুত প্রস্তুতি টুলস",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickTile(
                        title = "বিগত সালের প্রশ্ন",
                        icon = Icons.Filled.HistoryEdu,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onOpenQuestionBank,
                        tag = "home_tile_question_bank",
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "পরীক্ষার সাজেশন",
                        icon = Icons.Filled.Lightbulb,
                        containerColor = GoldSoft,
                        onClick = onOpenSuggestions,
                        tag = "home_tile_suggestions",
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "MCQ পরীক্ষা",
                        icon = Icons.Filled.Quiz,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = onOpenMcqExam,
                        tag = "home_tile_mcq",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickTile(
                        title = "PDF লাইব্রেরি",
                        icon = Icons.Filled.PictureAsPdf,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = onOpenLibrary,
                        tag = "home_tile_library",
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "স্টাডি প্ল্যানার",
                        icon = Icons.Filled.EventNote,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onOpenStudyPlanner,
                        tag = "home_tile_planner",
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "গুরুত্বপূর্ণ প্রশ্ন",
                        icon = Icons.Filled.Star,
                        containerColor = GoldSoft,
                        onClick = onOpenQuestionBank,
                        tag = "home_tile_important",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Department Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner_1794931530369),
                        contentDescription = "Political Science Department",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DeveloperConstants.DEPARTMENT,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${DeveloperConstants.UNIVERSITY} • ${papers.size}টি যাচাইকৃত কোর্স",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 6. Announcements
        if (announcements.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Campaign,
                            contentDescription = "নোটিশ",
                            tint = GoldAccent
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "নোটিশ ও আপডেট",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    announcements.take(2).forEach { notice ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (notice.isImportant) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                } else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = notice.titleBn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = notice.bodyBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Latest & Recently Viewed Materials
        val displayMaterials = (recentMaterials + latestMaterials).distinctBy { it.id }.take(5)
        if (displayMaterials.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "সাম্প্রতিক ও সর্বশেষ স্টাডি ম্যাটেরিয়াল",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayMaterials, key = { it.id }) { mat ->
                            Card(
                                onClick = { onOpenMaterial(mat) },
                                modifier = Modifier
                                    .width(240.dp)
                                    .testTag("home_material_card_${mat.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${mat.type} • ${mat.paperCode}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = mat.titleBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = mat.summaryBn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Favorite / Bookmarked Questions Preview
        if (bookmarkedQuestions.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = "প্রিয় প্রশ্ন",
                            tint = GoldAccent
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "সংরক্ষিত প্রিয় প্রশ্ন (${bookmarkedQuestions.size})",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    bookmarkedQuestions.take(3).forEach { q ->
                        Card(
                            onClick = onOpenQuestionBank,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Bookmark, contentDescription = null, tint = GoldAccent)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = q.questionBn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AcademicYearCard(
    year: AcademicYearLevel,
    paperCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag("home_year_card_${year.yearNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = RoyalBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = year.titleBn,
                            tint = RoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "$paperCount পত্র",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = year.titleBn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = year.titleEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickTile(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(92.dp)
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NavyPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = NavyDeep,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

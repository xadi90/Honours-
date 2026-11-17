package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ChapterEntity
import com.example.data.local.PaperEntity
import com.example.data.model.AdminRole
import com.example.data.model.DeveloperConstants
import com.example.data.model.QuestionCategory
import com.example.data.model.SuggestionCategory
import com.example.data.prefs.AiSettingsState
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.GoldSoft

@Composable
fun AdminPanelScreen(
    papers: List<PaperEntity>,
    chapters: List<ChapterEntity>,
    questionCount: Int,
    mcqCount: Int,
    aiSettings: AiSettingsState,
    onAddPaper: (Int, String, String, String, Int) -> Unit,
    onAddChapter: (Long, Int, String, String, String, String, String) -> Unit,
    onAddQuestion: (Long, Long, String, String, String, Boolean, String) -> Unit,
    onAddSuggestion: (Int, String, String, String, String, String) -> Unit,
    onAddAnnouncement: (String, String, Boolean) -> Unit,
    onUpdateAiSettings: (Int, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isUnlocked by rememberSaveable { mutableStateOf(false) }
    var pinInput by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf(AdminRole.SUPER_ADMIN) }
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "অ্যাডমিন কন্টেন্ট ড্যাশবোর্ড",
            subtitle = if (isUnlocked) "ভূমিকা: ${selectedRole.titleEn}" else "নিরাপদ প্রবেশাধিকার",
            onBack = onBack
        )

        if (!isUnlocked) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(10.dp))
                Text("ভূমিকাভিত্তিক অ্যাডমিন প্রবেশ (RBAC)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("অ্যাডমিন ভূমিকা নির্বাচন করুন ও পিন কোড (ডেমো পিন: 2026) প্রদান করুন।", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(14.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AdminRole.entries) { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role.titleEn) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("অ্যাডমিন পিন (2026)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (pinInput.trim() == "2026") {
                            isUnlocked = true
                        } else {
                            Toast.makeText(context, "সঠিক পিন কোড (2026) দিন", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_unlock_button")
                ) {
                    Text("অ্যাডমিন প্যানেলে প্রবেশ করুন")
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Analytics Overview
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("অ্যাপ্লিকেশন পরিসংখ্যান", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "মোট পত্র: ${papers.size} • অধ্যায়: ${chapters.size} • প্রশ্নোত্তর: $questionCount • MCQ: $mcqCount\nআজকের AI ব্যবহার: ${aiSettings.usedToday}/${aiSettings.dailyLimit}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Locked Developer Information Notice
            item {
                Surface(
                    color = GoldSoft,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("স্থায়ী ডেভেলপার তথ্য (পরিবর্তনযোগ্য নয়)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${DeveloperConstants.DEVELOPER_NAME} • ${DeveloperConstants.DEVELOPER_PHONE} • ${DeveloperConstants.DEVELOPER_EMAIL}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Section Selector Chips
            item {
                val sections = listOf("প্রশ্নোত্তর যোগ", "পত্র/অধ্যায়", "সাজেশন/নোটিশ", "AI সেটিংস")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sections.indices.toList()) { idx ->
                        val allowed = when (idx) {
                            1 -> selectedRole.canEditStructure
                            3 -> selectedRole.canManageSettings
                            else -> true
                        }
                        if (allowed) {
                            FilterChip(
                                selected = selectedSection == idx,
                                onClick = { selectedSection = idx },
                                label = { Text(sections[idx]) }
                            )
                        }
                    }
                }
            }

            when (selectedSection) {
                0 -> item {
                    var selectedPaperId by remember { mutableStateOf(papers.firstOrNull()?.id ?: 1L) }
                    val paperChapters = chapters.filter { it.paperId == selectedPaperId }
                    var selectedChapterId by remember(paperChapters) { mutableStateOf(paperChapters.firstOrNull()?.id ?: 1L) }
                    var qText by remember { mutableStateOf("") }
                    var aText by remember { mutableStateOf("") }
                    var isPast by remember { mutableStateOf(false) }
                    var examYr by remember { mutableStateOf("") }

                    Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("নতুন প্রশ্ন ও উত্তর যুক্ত করুন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(papers.take(12)) { p ->
                                    FilterChip(
                                        selected = selectedPaperId == p.id,
                                        onClick = { selectedPaperId = p.id },
                                        label = { Text(p.code) }
                                    )
                                }
                            }
                            if (paperChapters.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(paperChapters) { ch ->
                                        FilterChip(
                                            selected = selectedChapterId == ch.id,
                                            onClick = { selectedChapterId = ch.id },
                                            label = { Text("অধ্যায় ${ch.chapterNumber}") }
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(value = qText, onValueChange = { qText = it }, label = { Text("প্রশ্ন (বাংলা)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = aText, onValueChange = { aText = it }, label = { Text("কাঠামোবদ্ধ উত্তর") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = isPast, onCheckedChange = { isPast = it })
                                Spacer(Modifier.width(8.dp))
                                Text("প্রকৃত বিগত পরীক্ষার প্রশ্ন")
                            }
                            if (isPast) {
                                OutlinedTextField(value = examYr, onValueChange = { examYr = it }, label = { Text("পরীক্ষার সাল (যেমন: NU 2023)") }, modifier = Modifier.fillMaxWidth())
                            }
                            Button(
                                onClick = {
                                    onAddQuestion(selectedPaperId, selectedChapterId, QuestionCategory.SHORT.name, qText, aText, isPast, examYr)
                                    qText = ""; aText = ""; examYr = ""
                                    Toast.makeText(context, "প্রশ্নোত্তর যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("প্রশ্নোত্তর প্রকাশ করুন") }
                        }
                    }
                }

                1 -> item {
                    var yr by remember { mutableIntStateOf(1) }
                    var code by remember { mutableStateOf("") }
                    var tEn by remember { mutableStateOf("") }
                    var tBn by remember { mutableStateOf("") }
                    Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("নতুন কোর্স / পত্র যোগ করুন (Super Admin / Manager)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (1..4).forEach { y ->
                                    FilterChip(selected = yr == y, onClick = { yr = y }, label = { Text("${y}ম বর্ষ") })
                                }
                            }
                            OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("পত্র কোড (যেমন: 241919)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = tBn, onValueChange = { tBn = it }, label = { Text("শিরোনাম (বাংলা)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = tEn, onValueChange = { tEn = it }, label = { Text("শিরোনাম (English)") }, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = {
                                    onAddPaper(yr, code, tEn, tBn, 4)
                                    code = ""; tBn = ""; tEn = ""
                                    Toast.makeText(context, "নতুন পত্র যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("কোর্স সংরক্ষণ করুন") }
                        }
                    }
                }

                2 -> item {
                    var sTitle by remember { mutableStateOf("") }
                    var sBody by remember { mutableStateOf("") }
                    var aTitle by remember { mutableStateOf("") }
                    var aBody by remember { mutableStateOf("") }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("নতুন সাজেশন প্রকাশ করুন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = sTitle, onValueChange = { sTitle = it }, label = { Text("সাজেশনের শিরোনাম") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = sBody, onValueChange = { sBody = it }, label = { Text("প্রশ্নসমূহ") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                                Button(onClick = {
                                    onAddSuggestion(1, "211901", "রাজনৈতিক প্রতিষ্ঠান ও সংগঠন", SuggestionCategory.IMPORTANT.name, sTitle, sBody)
                                    sTitle = ""; sBody = ""
                                    Toast.makeText(context, "সাজেশন প্রকাশিত হয়েছে", Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.fillMaxWidth()) { Text("সাজেশন প্রকাশ") }
                            }
                        }
                        Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("নতুন নোটিশ (Announcement) প্রকাশ করুন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = aTitle, onValueChange = { aTitle = it }, label = { Text("নোটিশ শিরোনাম") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = aBody, onValueChange = { aBody = it }, label = { Text("বিস্তারিত বার্তা") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                                Button(onClick = {
                                    onAddAnnouncement(aTitle, aBody, true)
                                    aTitle = ""; aBody = ""
                                    Toast.makeText(context, "নোটিশ প্রকাশিত হয়েছে", Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.fillMaxWidth()) { Text("নোটিশ প্রকাশ") }
                            }
                        }
                    }
                }

                3 -> item {
                    var limitText by remember { mutableStateOf(aiSettings.dailyLimit.toString()) }
                    var imgEnabled by remember { mutableStateOf(aiSettings.imageSolverEnabled) }
                    Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("AI ব্যবহার ও সুরক্ষা সেটিংস (Super Admin)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("প্রোভাইডার: ${aiSettings.providerLabel}", style = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(value = limitText, onValueChange = { limitText = it }, label = { Text("দৈনিক ব্যবহারকারীর সীমা (Requests/Day)") }, modifier = Modifier.fillMaxWidth())
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = imgEnabled, onCheckedChange = { imgEnabled = it })
                                Spacer(Modifier.width(8.dp))
                                Text("ছবি ও OCR প্রশ্ন সমাধান সক্রিয়")
                            }
                            Button(onClick = {
                                onUpdateAiSettings(limitText.toIntOrNull() ?: 25, imgEnabled)
                                Toast.makeText(context, "AI সেটিংস আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                            }, modifier = Modifier.fillMaxWidth()) { Text("সেটিংস সংরক্ষণ") }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.AiConversationEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.QuestionAnswerEntity
import com.example.data.local.StudyMaterialEntity
import com.example.data.model.AcademicYearLevel
import com.example.data.model.DeveloperConstants
import com.example.data.prefs.NotificationPrefs
import com.example.data.prefs.StudentProfileState
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.GoldAccent

@Composable
fun ProfileScreen(
    profile: StudentProfileState,
    themeMode: AppThemeMode,
    notificationPrefs: NotificationPrefs,
    bookmarkedQuestions: List<QuestionAnswerEntity>,
    savedQuestions: List<QuestionAnswerEntity>,
    offlineMaterials: List<StudyMaterialEntity>,
    aiConversations: List<AiConversationEntity>,
    chapters: List<ChapterEntity>,
    offlineStorageKb: Long,
    onUpdateProfile: (String, Int, String, String, Boolean) -> Unit,
    onUpdateAvatar: (String) -> Unit,
    onLogoutToGuest: () -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onUpdateNotificationPrefs: (NotificationPrefs) -> Unit,
    onOpenQuestionBank: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAiSolver: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onUpdateAvatar(uri.toString())
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "নোটিফিকেশন অনুমতি চালু হয়েছে", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "নোটিফিকেশন অনুমতি দেওয়া হয়নি", Toast.LENGTH_SHORT).show()
        }
    }

    val completedChapters = chapters.count { it.isCompleted }

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "শিক্ষার্থী প্রোফাইল ও সেটিংস",
            subtitle = if (profile.isGuestMode) "গেস্ট মোড সক্রিয়" else profile.email
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Profile Identity Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profile.avatarUri.isNotBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(profile.avatarUri),
                                        contentDescription = "প্রোফাইল ছবি",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Filled.Person, contentDescription = "প্রোফাইল", modifier = Modifier.size(36.dp))
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = GoldAccent,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(22.dp)
                                ) {
                                    Icon(Icons.Filled.CameraAlt, contentDescription = "ছবি পরিবর্তন", modifier = Modifier.padding(4.dp))
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(profile.studentName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(AcademicYearLevel.fromYear(profile.academicYear).titleBn, style = MaterialTheme.typography.bodyMedium)
                                Text(profile.collegeName, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.testTag("profile_edit_button")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "প্রোফাইল সম্পাদনা")
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (profile.isGuestMode) {
                                Button(
                                    onClick = { showEditProfileDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("profile_register_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("নিবন্ধন / লগইন")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onLogoutToGuest,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("লগআউট")
                                }
                            }
                            OutlinedButton(
                                onClick = { showPasswordResetDialog = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.LockReset, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("পাসওয়ার্ড রিসেট")
                            }
                        }
                    }
                }
            }

            // 2. Academic & Offline Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBox("বুকমার্ক", "${bookmarkedQuestions.size}", Icons.Filled.Bookmark, Modifier.weight(1f), onOpenQuestionBank)
                    StatBox("সংরক্ষিত", "${savedQuestions.size + offlineMaterials.size}", Icons.Filled.DownloadDone, Modifier.weight(1f), onOpenLibrary)
                    StatBox("AI চ্যাট", "${aiConversations.size}", Icons.Filled.History, Modifier.weight(1f), onOpenAiSolver)
                }
            }

            // 3. Offline Storage & Progress Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("অফলাইন স্টোরেজ ও পড়াশোনার অগ্রগতি", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "অফলাইন PDF: ${offlineMaterials.size}টি ($offlineStorageKb KB) • সম্পন্ন অধ্যায়: $completedChapters/${chapters.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. Theme Settings
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DarkMode, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("থিম সেটিংস (Theme)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { onSetThemeMode(mode) },
                                    label = { Text(mode.labelBn) },
                                    modifier = Modifier.testTag("theme_chip_${mode.name}")
                                )
                            }
                        }
                    }
                }
            }

            // 5. Notification Preferences
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notifications, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("নোটিফিকেশন সেটিংস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        NotifSwitchRow("নতুন নোট ও উত্তর", notificationPrefs.newNotes) {
                            onUpdateNotificationPrefs(notificationPrefs.copy(newNotes = it))
                        }
                        NotifSwitchRow("নতুন সাজেশন ও প্রশ্নপত্র", notificationPrefs.newSuggestions) {
                            onUpdateNotificationPrefs(notificationPrefs.copy(newSuggestions = it))
                        }
                        NotifSwitchRow("গুরুত্বপূর্ণ একাডেমিক নোটিশ", notificationPrefs.examNotices) {
                            onUpdateNotificationPrefs(notificationPrefs.copy(examNotices = it))
                        }
                        NotifSwitchRow("দৈনিক পড়াশোনার রিমাইন্ডার", notificationPrefs.studyReminders) { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val granted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!granted) notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            onUpdateNotificationPrefs(notificationPrefs.copy(studyReminders = enabled))
                        }
                    }
                }
            }

            // 6. Admin Panel & About App
            item {
                Card(
                    onClick = onOpenAdminPanel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_admin_panel_card"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("কনটেন্ট অ্যাডমিন প্যানেল (Admin Studio)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("কোর্স, অধ্যায়, প্রশ্নোত্তর, সাজেশন ও AI সেটিংস পরিচালনা", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }

            item {
                Card(
                    onClick = onOpenAbout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_about_card"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = GoldAccent)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("অ্যাপ ও ডেভেলপার পরিচিতি (About App)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${DeveloperConstants.DEVELOPER_NAME} • ${DeveloperConstants.DEVELOPER_PHONE}", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }

            // Permanent immutable developer info footer in Application Settings
            item {
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${DeveloperConstants.APP_NAME} v${DeveloperConstants.APP_VERSION}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Developer: ${DeveloperConstants.DEVELOPER_NAME} | Phone: ${DeveloperConstants.DEVELOPER_PHONE}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Email: ${DeveloperConstants.DEVELOPER_EMAIL}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(if (profile.isGuestMode) "" else profile.studentName) }
        var year by remember { mutableIntStateOf(profile.academicYear) }
        var college by remember { mutableStateOf(profile.collegeName) }
        var email by remember { mutableStateOf(profile.email) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("শিক্ষার্থী প্রোফাইল ও নিবন্ধন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("আপনার নাম") })
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("ইমেইল (ঐচ্ছিক)") })
                    OutlinedTextField(value = college, onValueChange = { college = it }, label = { Text("কলেজের নাম") })
                    Text("অনার্স বর্ষ নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..4).forEach { yr ->
                            FilterChip(
                                selected = year == yr,
                                onClick = { year = yr },
                                label = { Text("${yr}ম") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateProfile(name, year, college, email, name.isBlank())
                    showEditProfileDialog = false
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("বাতিল") }
            }
        )
    }

    if (showPasswordResetDialog) {
        var resetEmail by remember { mutableStateOf(profile.email) }
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            title = { Text("পাসওয়ার্ড রিসেট (Password Recovery)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ক্লাউড অ্যাকাউন্ট রিকভারির জন্য আপনার নিবন্ধিত ইমেইল লিখুন। (বর্তমানে লোকাল প্রোফাইল সক্রিয় রয়েছে।)")
                    OutlinedTextField(value = resetEmail, onValueChange = { resetEmail = it }, label = { Text("ইমেইল") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    Toast.makeText(context, "রিকভারি নির্দেশনা প্রস্তুত করা হয়েছে", Toast.LENGTH_SHORT).show()
                    showPasswordResetDialog = false
                }) { Text("পাঠান") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) { Text("বন্ধ") }
            }
        )
    }
}

@Composable
private fun StatBox(
    label: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NotifSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

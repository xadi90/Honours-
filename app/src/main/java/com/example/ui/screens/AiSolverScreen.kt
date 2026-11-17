package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.AiConversationEntity
import com.example.data.local.AiMessageEntity
import com.example.data.local.PaperEntity
import com.example.data.model.AcademicYearLevel
import com.example.data.model.AiQuickTool
import com.example.data.model.AnswerLengthOption
import com.example.data.model.QuestionCategory
import com.example.ui.components.ErrorStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.NavyDeep
import com.example.ui.viewmodel.AiSolverUiState
import com.example.ui.viewmodel.AiSolverViewModel
import com.example.ui.viewmodel.AiUiStatus
import com.example.util.ImageUtils
import com.example.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiSolverScreen(
    viewModel: AiSolverViewModel,
    uiState: AiSolverUiState,
    messages: List<AiMessageEntity>,
    conversations: List<AiConversationEntity>,
    papers: List<PaperEntity>,
    isImageSolverEnabled: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inputTab by rememberSaveable { mutableIntStateOf(0) } // 0 = Text, 1 = Image
    var showHistorySheet by remember { mutableStateOf(false) }
    var editingImageIndex by remember { mutableStateOf<Int?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val yearPapers = remember(papers, uiState.selectedYear) {
        papers.filter { it.yearNumber == uiState.selectedYear }
    }

    // Photo Picker (Zero-permission per Google Play Policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        uris.forEach { uri ->
            ImageUtils.decodeSampledBitmap(context, uri)?.let { bmp ->
                viewModel.addImage(bmp)
            }
        }
    }

    // Camera capture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            ImageUtils.decodeSampledBitmap(context, uri)?.let { bmp ->
                viewModel.addImage(bmp)
            } ?: Toast.makeText(context, "ছবিটি লোড করা যায়নি", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = ImageUtils.createTempCameraUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "ক্যামেরা অনুমতি প্রয়োজন। আপনি গ্যালারি থেকে ছবি নির্বাচন করতে পারেন।", Toast.LENGTH_LONG).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "AI Question Solver",
            subtitle = "আপনার পড়াশোনার যেকোনো প্রশ্নের সমাধান পান AI-এর মাধ্যমে।",
            actions = {
                IconButton(
                    onClick = { viewModel.startNewSession() },
                    modifier = Modifier.testTag("ai_new_session_button")
                ) {
                    Icon(Icons.Filled.AddComment, contentDescription = "নতুন প্রশ্ন")
                }
                IconButton(
                    onClick = { showHistorySheet = true },
                    modifier = Modifier.testTag("ai_history_button")
                ) {
                    Icon(Icons.Filled.History, contentDescription = "পূর্ববর্তী চ্যাট")
                }
            }
        )

        // Transparent AI Status Notice
        Surface(
            color = GoldSoft,
            contentColor = NavyDeep,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = viewModel.noticeBannerBn,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Input Section (Text vs Image)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Column {
                        TabRow(selectedTabIndex = inputTab) {
                            Tab(
                                selected = inputTab == 0,
                                onClick = { inputTab = 0 },
                                text = { Text("প্রশ্ন লিখুন") },
                                modifier = Modifier.testTag("ai_tab_text")
                            )
                            Tab(
                                selected = inputTab == 1,
                                onClick = { inputTab = 1 },
                                text = { Text("ছবি আপলোড (${uiState.attachedImages.size})") },
                                modifier = Modifier.testTag("ai_tab_image")
                            )
                        }

                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Year selector chips
                            Text("অনার্স বর্ষ ও পত্র নির্বাচন করুন", style = MaterialTheme.typography.labelLarge)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(AcademicYearLevel.entries) { yr ->
                                    FilterChip(
                                        selected = uiState.selectedYear == yr.yearNumber,
                                        onClick = {
                                            val firstPaper = papers.firstOrNull { it.yearNumber == yr.yearNumber }
                                            val defaultTitle = firstPaper?.let { "${it.titleBn} (${it.code})" } ?: yr.titleBn
                                            viewModel.selectYear(yr.yearNumber, defaultTitle)
                                        },
                                        label = { Text(yr.shortBn) },
                                        modifier = Modifier.testTag("ai_year_chip_${yr.yearNumber}")
                                    )
                                }
                            }

                            // Paper Dropdown
                            PaperSelectorDropdown(
                                selectedPaper = uiState.selectedPaperTitle,
                                yearPapers = yearPapers,
                                onSelectPaper = { viewModel.selectPaper("${it.titleBn} (${it.code})") }
                            )

                            // Question Type & Answer Length
                            Text("উত্তরের দৈর্ঘ্য নির্বাচন করুন", style = MaterialTheme.typography.labelLarge)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(AnswerLengthOption.entries) { len ->
                                    FilterChip(
                                        selected = uiState.selectedAnswerLength == len,
                                        onClick = { viewModel.selectAnswerLength(len) },
                                        label = { Text(len.labelBn) },
                                        modifier = Modifier.testTag("ai_length_chip_${len.name}")
                                    )
                                }
                            }

                            Text("প্রশ্নের ধরন", style = MaterialTheme.typography.labelLarge)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(QuestionCategory.entries) { cat ->
                                    FilterChip(
                                        selected = uiState.selectedQuestionType == cat,
                                        onClick = { viewModel.selectQuestionType(cat) },
                                        label = { Text(cat.labelBn) }
                                    )
                                }
                            }

                            if (inputTab == 0) {
                                OutlinedTextField(
                                    value = uiState.questionInput,
                                    onValueChange = viewModel::updateQuestionInput,
                                    label = { Text("আপনার প্রশ্ন লিখুন বা পেস্ট করুন (বাংলা / English)") },
                                    placeholder = { Text("উদাহরণ: ম্যাক্স ওয়েবারের আমলাতন্ত্র তত্ত্বের বৈশিষ্ট্য ও সমালোচনা লেখো।") },
                                    minLines = 4,
                                    maxLines = 7,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_question_input")
                                )
                            } else {
                                if (!isImageSolverEnabled) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "অ্যাডমিন সেটিংস থেকে বর্তমানে ছবি প্রশ্ন সমাধান সাময়িকভাবে বন্ধ রাখা হয়েছে।",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val granted = ContextCompat.checkSelfPermission(
                                                    context, Manifest.permission.CAMERA
                                                ) == PackageManager.PERMISSION_GRANTED
                                                if (granted) {
                                                    val uri = ImageUtils.createTempCameraUri(context)
                                                    pendingCameraUri = uri
                                                    takePictureLauncher.launch(uri)
                                                } else {
                                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("ai_camera_button"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                                            Spacer(Modifier.width(6.dp))
                                            Text("ক্যামেরা")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("ai_gallery_button"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                                            Spacer(Modifier.width(6.dp))
                                            Text("গ্যালারি")
                                        }
                                    }

                                    if (uiState.attachedImages.isNotEmpty()) {
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            itemsIndexed(uiState.attachedImages) { index, bmp ->
                                                Card(
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.size(width = 136.dp, height = 160.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        Image(
                                                            bitmap = bmp.asImageBitmap(),
                                                            contentDescription = "আপলোডকৃত প্রশ্ন ছবি ${index + 1}",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                        Surface(
                                                            color = Color.Black.copy(alpha = 0.65f),
                                                            shape = CircleShape,
                                                            modifier = Modifier
                                                                .align(Alignment.TopEnd)
                                                                .padding(4.dp)
                                                        ) {
                                                            IconButton(
                                                                onClick = { viewModel.removeImageAt(index) },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Filled.Close,
                                                                    contentDescription = "ছবি সরান",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                        Row(
                                                            modifier = Modifier
                                                                .align(Alignment.BottomCenter)
                                                                .fillMaxWidth()
                                                                .background(Color.Black.copy(alpha = 0.6f)),
                                                            horizontalArrangement = Arrangement.SpaceEvenly
                                                        ) {
                                                            IconButton(
                                                                onClick = { viewModel.rotateImageAt(index) },
                                                                modifier = Modifier.size(36.dp)
                                                            ) {
                                                                Icon(Icons.Filled.RotateRight, contentDescription = "ঘোরান", tint = Color.White, modifier = Modifier.size(18.dp))
                                                            }
                                                            IconButton(
                                                                onClick = { editingImageIndex = index },
                                                                modifier = Modifier.size(36.dp)
                                                            ) {
                                                                Icon(Icons.Filled.Crop, contentDescription = "ক্রপ করুন", tint = Color.White, modifier = Modifier.size(18.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = uiState.questionInput,
                                        onValueChange = viewModel::updateQuestionInput,
                                        label = { Text("ছবি সম্পর্কিত অতিরিক্ত নির্দেশনা (ঐচ্ছিক)") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.clearQuestionInput() },
                                    modifier = Modifier.testTag("ai_clear_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Clear, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("পরিষ্কার")
                                }
                                Button(
                                    onClick = { viewModel.submitQuestion(isFollowUp = false) },
                                    enabled = uiState.status !is AiUiStatus.Loading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("ai_send_question_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("AI সমাধান নিন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Status / Error / Streaming Output
            when (val status = uiState.status) {
                is AiUiStatus.Loading -> {
                    item {
                        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(14.dp))
                                Text("প্রশ্ন বিশ্লেষণ ও একাডেমিক উত্তর তৈরি হচ্ছে...", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                is AiUiStatus.Streaming -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("AI উত্তর লিখছে...", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.height(8.dp))
                                Text(text = status.partialText, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                is AiUiStatus.Error -> {
                    item {
                        ErrorStateView(
                            title = if (status.isUnreadableImage) "অস্পষ্ট ছবি শনাক্ত হয়েছে" else "AI সমাধানে সমস্যা",
                            message = status.message,
                            onRetry = { viewModel.submitQuestion(isFollowUp = false) }
                        )
                    }
                }
                else -> Unit
            }

            // 3. Conversation Messages Thread
            if (messages.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("সমাধান ও কথোপকথন", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "${AcademicYearLevel.fromYear(uiState.selectedYear).titleBn} • ${uiState.selectedPaperTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleCurrentBookmark() },
                            modifier = Modifier.testTag("ai_bookmark_conversation")
                        ) {
                            Icon(
                                imageVector = if (uiState.isCurrentBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "সংরক্ষণ করুন",
                                tint = if (uiState.isCurrentBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.role == "user"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else MaterialTheme.colorScheme.surface
                        ),
                        border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)) else null
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isUser) "আপনার প্রশ্ন" else "AI একাডেমিক সমাধান",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = msg.contentText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (!isUser) {
                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(onClick = {
                                        PdfExporter.copyToClipboard(context, "AI Answer", msg.contentText)
                                        Toast.makeText(context, "উত্তর কপি হয়েছে", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "কপি করুন")
                                    }
                                    IconButton(onClick = {
                                        PdfExporter.sharePlainText(context, uiState.lastOriginalQuestion, msg.contentText)
                                    }) {
                                        Icon(Icons.Filled.Share, contentDescription = "শেয়ার করুন")
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            val res = PdfExporter.exportAnswerToPdf(
                                                context = context,
                                                fileNamePrefix = "ai_solution_${msg.id}",
                                                title = uiState.lastOriginalQuestion.ifBlank { "AI Question Solver" },
                                                subtitle = "${AcademicYearLevel.fromYear(uiState.selectedYear).titleBn} • ${uiState.selectedPaperTitle}",
                                                bodyText = msg.contentText
                                            )
                                            res.onSuccess { file -> PdfExporter.sharePdfFile(context, file, "AI Solution") }
                                        }
                                    }) {
                                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "PDF এক্সপোর্ট")
                                    }
                                    IconButton(onClick = { viewModel.regenerateLastAnswer() }) {
                                        Icon(Icons.Filled.Refresh, contentDescription = "পুনরায় তৈরি")
                                    }
                                }
                            }
                        }
                    }
                }

                // 10 Additional AI Tools
                item {
                    Column {
                        Text("অতিরিক্ত AI টুলস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AiQuickTool.entries.forEach { tool ->
                                AssistChip(
                                    onClick = { viewModel.applyQuickTool(tool) },
                                    label = { Text(tool.labelBn) },
                                    modifier = Modifier.testTag("ai_tool_${tool.name}")
                                )
                            }
                        }
                    }
                }

                // Conversational Follow-up Input
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.followUpInput,
                            onValueChange = viewModel::updateFollowUpInput,
                            placeholder = { Text("ফলো-আপ প্রশ্ন করুন (যেমন: এর বৈশিষ্ট্যগুলো লিখুন)...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_followup_input")
                        )
                        Button(
                            onClick = { viewModel.submitQuestion(isFollowUp = true) },
                            enabled = uiState.followUpInput.isNotBlank() && uiState.status !is AiUiStatus.Loading,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("ai_followup_send")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "পাঠান")
                        }
                    }
                }
            }
        }
    }

    // Crop & Rotate Image Dialog
    val idx = editingImageIndex
    if (idx != null && idx in uiState.attachedImages.indices) {
        val bmp = uiState.attachedImages[idx]
        AlertDialog(
            onDismissRequest = { editingImageIndex = null },
            title = { Text("ছবি সম্পাদনা (Crop & Rotate)") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "সম্পাদনার জন্য ছবি",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.rotateImageAt(idx) }) {
                            Icon(Icons.Filled.RotateRight, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("৯০° ঘোরান")
                        }
                        OutlinedButton(onClick = { viewModel.cropImageAt(idx) }) {
                            Icon(Icons.Filled.Crop, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("মার্জিন ক্রপ")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.cropImageAt(idx, 1, 1) }) { Text("১:১ বর্গ") }
                        TextButton(onClick = { viewModel.cropImageAt(idx, 4, 3) }) { Text("৪:৩ ফ্রেম") }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { editingImageIndex = null }) { Text("সম্পন্ন") }
            }
        )
    }

    // Saved AI Chat History Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AI চ্যাট ইতিহাস (অফলাইনেও পড়া যাবে)", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                if (conversations.isEmpty()) {
                    Text("এখনো কোনো AI কথোপকথন সংরক্ষিত নেই।", style = MaterialTheme.typography.bodyMedium)
                } else {
                    conversations.forEach { conv ->
                        Card(
                            onClick = {
                                viewModel.loadConversation(conv)
                                showHistorySheet = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(conv.titleBn, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${conv.paperTitleBn} • ${conv.answerLength}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { viewModel.deleteConversation(conv.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "মুছুন")
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaperSelectorDropdown(
    selectedPaper: String,
    yearPapers: List<PaperEntity>,
    onSelectPaper: (PaperEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedPaper,
            onValueChange = {},
            readOnly = true,
            label = { Text("পত্র (Paper)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            yearPapers.forEach { paper ->
                DropdownMenuItem(
                    text = { Text("${paper.code} — ${paper.titleBn}") },
                    onClick = {
                        onSelectPaper(paper)
                        expanded = false
                    }
                )
            }
        }
    }
}

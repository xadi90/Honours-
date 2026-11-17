package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.StudyMaterialEntity
import com.example.data.model.AcademicYearLevel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.util.PdfExporter
import java.io.File

@Composable
fun LibraryScreen(
    materials: List<StudyMaterialEntity>,
    offlineStorageKb: Long,
    onOpenMaterial: (StudyMaterialEntity) -> Unit,
    onToggleDownload: (StudyMaterialEntity, Boolean) -> Unit,
    onBack: () -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }
    var onlyOffline by rememberSaveable { mutableStateOf(false) }

    val filtered = remember(materials, search, selectedType, onlyOffline) {
        materials.filter { m ->
            val matchType = selectedType == null || m.type == selectedType
            val matchOffline = !onlyOffline || m.isDownloadedOffline
            val matchSearch = search.isBlank() ||
                m.titleBn.contains(search, ignoreCase = true) ||
                m.paperCode.contains(search, ignoreCase = true) ||
                m.summaryBn.contains(search, ignoreCase = true)
            matchType && matchOffline && matchSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "PDF ও স্টাডি লাইব্রেরি",
            subtitle = "অফলাইন ডকুমেন্ট স্টোরেজ: $offlineStorageKb KB",
            onBack = onBack
        )

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("নোট বা PDF খুঁজুন (যেমন: 211901, সংবিধান)...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("library_search_input")
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedType == null && !onlyOffline,
                    onClick = { selectedType = null; onlyOffline = false },
                    label = { Text("সব (${materials.size})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedType == "PDF",
                    onClick = { selectedType = if (selectedType == "PDF") null else "PDF" },
                    label = { Text("PDF ডকুমেন্ট") }
                )
            }
            item {
                FilterChip(
                    selected = selectedType == "NOTE",
                    onClick = { selectedType = if (selectedType == "NOTE") null else "NOTE" },
                    label = { Text("টেক্সট নোট") }
                )
            }
            item {
                FilterChip(
                    selected = onlyOffline,
                    onClick = { onlyOffline = !onlyOffline },
                    label = { Text("অফলাইন ডাউনলোডকৃত") }
                )
            }
        }

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "কোনো ডকুমেন্ট পাওয়া যায়নি",
                message = "অনুমোদিত ডকুমেন্ট যুক্ত হলে বা ফিল্টার পরিবর্তন করলে এখানে প্রদর্শিত হবে।",
                icon = Icons.Filled.PictureAsPdf
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { mat ->
                    Card(
                        onClick = { onOpenMaterial(mat) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("library_item_${mat.id}"),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = "${mat.type} • ${mat.paperCode}",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "${AcademicYearLevel.fromYear(mat.yearNumber).shortBn} • ~${mat.fileSizeKb} KB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(mat.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(mat.summaryBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = { onToggleDownload(mat, !mat.isDownloadedOffline) },
                                modifier = Modifier.testTag("library_download_${mat.id}")
                            ) {
                                Icon(
                                    imageVector = if (mat.isDownloadedOffline) Icons.Filled.DownloadDone else Icons.Outlined.DownloadForOffline,
                                    contentDescription = "অফলাইন ডাউনলোড",
                                    tint = if (mat.isDownloadedOffline) EmeraldSuccess else MaterialTheme.colorScheme.primary
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
fun PdfViewerScreen(
    material: StudyMaterialEntity?,
    onBack: () -> Unit,
    onBookmarkPage: (Long, Int) -> Unit,
    onToggleDownload: (StudyMaterialEntity, Boolean) -> Unit
) {
    val context = LocalContext.current
    var isFullScreen by rememberSaveable { mutableStateOf(false) }
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3.2f)
        offset = if (scale > 1f) offset + offsetChange else Offset.Zero
    }

    LaunchedEffect(material?.id) {
        if (material != null) {
            currentPage = material.bookmarkedPage.coerceAtLeast(0)
            val result = PdfExporter.ensureMaterialPdfExists(
                context = context,
                materialId = material.id,
                title = material.titleBn,
                subtitle = "পত্র কোড: ${material.paperCode} • অনার্স ${material.yearNumber}ম বর্ষ",
                bodyText = material.contentBodyBn
            )
            result.onSuccess { file ->
                pdfFile = file
                totalPages = PdfExporter.getPdfPageCount(file)
                if (currentPage >= totalPages) currentPage = 0
                pageBitmap = PdfExporter.renderPdfPage(file, currentPage)
                loadError = null
            }.onFailure { err ->
                loadError = err.localizedMessage ?: "ডকুমেন্ট লোড করা যায়নি।"
            }
        }
    }

    LaunchedEffect(pdfFile, currentPage) {
        val file = pdfFile ?: return@LaunchedEffect
        pageBitmap = PdfExporter.renderPdfPage(file, currentPage)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isFullScreen) {
            HonoursTopBar(
                title = material?.titleBn ?: "ডকুমেন্ট রিডার",
                subtitle = if (totalPages > 0) "পৃষ্ঠা ${currentPage + 1} / $totalPages" else material?.paperCode,
                onBack = onBack,
                actions = {
                    if (material != null) {
                        val isBookmarkedHere = material.bookmarkedPage == currentPage && totalPages > 0
                        IconButton(onClick = {
                            onBookmarkPage(material.id, currentPage)
                            Toast.makeText(context, "পৃষ্ঠা ${currentPage + 1} বুকমার্ক হয়েছে", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = if (isBookmarkedHere) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "পৃষ্ঠা বুকমার্ক",
                                tint = if (isBookmarkedHere) GoldAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = {
                            pdfFile?.let { PdfExporter.sharePdfFile(context, it, material.titleBn) }
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "PDF শেয়ার করুন")
                        }
                    }
                    IconButton(onClick = { isFullScreen = true }) {
                        Icon(Icons.Filled.Fullscreen, contentDescription = "ফুল স্ক্রিন")
                    }
                }
            )
        }

        if (material == null) {
            ErrorStateView(
                title = "ডকুমেন্ট খুঁজে পাওয়া যায়নি",
                message = "অনুগ্রহ করে লাইব্রেরিতে ফিরে গিয়ে আবার চেষ্টা করুন।",
                onBack = onBack
            )
            return@Column
        }

        if (loadError != null) {
            ErrorStateView(
                title = "ডকুমেন্ট লোড ব্যর্থ হয়েছে",
                message = loadError ?: "অজানা ত্রুটি",
                onBack = onBack
            )
            return@Column
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            val bmp = pageBitmap
            if (bmp == null) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "${material.titleBn} পৃষ্ঠা ${currentPage + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = transformState)
                )
            }

            if (isFullScreen) {
                Surface(
                    onClick = { isFullScreen = false },
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FullscreenExit,
                        contentDescription = "ফুল স্ক্রিন বন্ধ",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // PDF Controls Bar (Zoom In/Out, Page Navigation, Offline Save)
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "পূর্ববর্তী পৃষ্ঠা")
                    }
                    Text(
                        text = if (totalPages > 0) "${currentPage + 1}/$totalPages" else "-",
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(
                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                        enabled = currentPage < totalPages - 1
                    ) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "পরবর্তী পৃষ্ঠা")
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        scale = (scale - 0.35f).coerceAtLeast(1f)
                        if (scale == 1f) offset = Offset.Zero
                    }) {
                        Icon(Icons.Filled.ZoomOut, contentDescription = "জুম আউট")
                    }
                    IconButton(onClick = { scale = (scale + 0.35f).coerceAtMost(3f) }) {
                        Icon(Icons.Filled.ZoomIn, contentDescription = "জুম ইন")
                    }
                    OutlinedButton(
                        onClick = { onToggleDownload(material, !material.isDownloadedOffline) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(if (material.isDownloadedOffline) "অফলাইন ✓" else "সেভ করুন")
                    }
                }
            }
        }
    }
}

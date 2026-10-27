package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackState
import com.example.data.local.BookmarkEntity
import com.example.data.local.UserSettings
import com.example.data.model.AyatItem
import com.example.data.model.SurahDetail
import com.example.data.repository.ResourceState
import com.example.ui.components.BismillahHeader
import com.example.ui.components.ErrorStateView
import com.example.ui.components.GoldenMedallion
import com.example.ui.components.LoadingStateView
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ForestDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.IvoryBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    surahNumber: Int,
    targetVerseNumber: Int? = null,
    surahDetailState: ResourceState<SurahDetail>,
    audioState: AudioPlaybackState,
    settings: UserSettings,
    bookmarks: List<BookmarkEntity>,
    onLoadSurah: (Int) -> Unit,
    onBack: () -> Unit,
    onNavigateSurah: (Int) -> Unit,
    onNavigateToTafsir: (Int, Int?) -> Unit,
    onPlayVerseAudio: (SurahDetail, AyatItem) -> Unit,
    onToggleBookmark: (SurahDetail, AyatItem) -> Unit,
    onAddNote: (surahNo: Int, surahName: String, verseNo: Int, content: String) -> Unit,
    onMarkLastRead: (surahNo: Int, surahName: String, verseNo: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Dialog states
    var showJumpDialog by remember { mutableStateOf(false) }
    var noteTargetVerse by remember { mutableStateOf<AyatItem?>(null) }
    var noteInputText by remember { mutableStateOf("") }

    // Auto-scroll states
    var isAutoScrolling by remember { mutableStateOf(false) }
    var isAutoScrollPaused by remember { mutableStateOf(false) }
    var autoScrollSpeed by remember { mutableIntStateOf(settings.autoScrollSpeed) }

    // Load initial data
    LaunchedEffect(surahNumber) {
        onLoadSurah(surahNumber)
    }

    // Scroll to target verse when loaded
    LaunchedEffect(surahDetailState, targetVerseNumber) {
        if (surahDetailState is ResourceState.Success && targetVerseNumber != null) {
            val index = surahDetailState.data.ayat.indexOfFirst { it.nomorAyat == targetVerseNumber }
            if (index >= 0) {
                // Header is at index 0 and 1, so offset by 2
                listState.animateScrollToItem((index + 2).coerceAtLeast(0))
            }
        }
    }

    // Auto-scroll loop with proper cleanup
    LaunchedEffect(isAutoScrolling, isAutoScrollPaused, autoScrollSpeed) {
        if (isAutoScrolling && !isAutoScrollPaused) {
            val pixelsPerStep = when (autoScrollSpeed) {
                1 -> 1.2f
                2 -> 2.2f
                3 -> 3.5f
                4 -> 5.0f
                5 -> 7.0f
                else -> 2.5f
            }
            while (isActive && isAutoScrolling && !isAutoScrollPaused) {
                // Check if reached end of list
                val canScrollFurther = listState.canScrollForward
                if (!canScrollFurther) {
                    isAutoScrolling = false
                    break
                }
                listState.scrollBy(pixelsPerStep)
                delay(30)
            }
        }
    }

    // Stop auto-scroll on exit or unmount
    DisposableEffect(Unit) {
        onDispose {
            isAutoScrolling = false
        }
    }

    BackHandler {
        if (isAutoScrolling) {
            isAutoScrolling = false
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (surahDetailState is ResourceState.Success) {
                        val s = surahDetailState.data
                        Column {
                            Text(
                                text = "${s.nomor}. ${s.namaLatin}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${s.tempatTurun} • ${s.jumlahAyat} Ayat • ${s.arti}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text("Membaca Surah $surahNumber")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    // Jump to verse dialog button
                    IconButton(onClick = { showJumpDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.FormatListNumbered,
                            contentDescription = "Lompat ke Ayat"
                        )
                    }
                    // Tafsir shortcut
                    IconButton(onClick = { onNavigateToTafsir(surahNumber, 1) }) {
                        Icon(
                            imageVector = Icons.Filled.AutoStories,
                            contentDescription = "Tafsir Surah"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = EmeraldPrimary
                )
            )
        },
        bottomBar = {
            // Floating Auto-Scroll Control Bar
            AutoScrollControlBar(
                isActive = isAutoScrolling,
                isPaused = isAutoScrollPaused,
                currentSpeed = autoScrollSpeed,
                onStart = {
                    isAutoScrolling = true
                    isAutoScrollPaused = false
                },
                onPause = {
                    isAutoScrollPaused = true
                },
                onResume = {
                    isAutoScrollPaused = false
                },
                onStop = {
                    isAutoScrolling = false
                    isAutoScrollPaused = false
                },
                onSpeedChanged = { autoScrollSpeed = it }
            )
        },
        modifier = modifier.testTag("reader_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (surahDetailState) {
                is ResourceState.Loading -> {
                    LoadingStateView(message = "Memuat ayat surah ke-$surahNumber...")
                }
                is ResourceState.Error -> {
                    ErrorStateView(
                        message = surahDetailState.message,
                        onRetry = { onLoadSurah(surahNumber) }
                    )
                }
                is ResourceState.Success -> {
                    val surah = surahDetailState.data

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        // Surah Arabic Banner
                        item {
                            SurahHeroBanner(surah = surah)
                        }

                        // Bismillah (Shown for all surahs except At-Taubah #9)
                        if (surah.nomor != 9) {
                            item {
                                BismillahHeader()
                            }
                        }

                        // Verses List
                        itemsIndexed(surah.ayat, key = { _, ayat -> "${surah.nomor}:${ayat.nomorAyat}" }) { _, ayat ->
                            val isCurrentlyPlaying = audioState.hasActiveTrack &&
                                    audioState.surahNumber == surah.nomor &&
                                    audioState.verseNumber == ayat.nomorAyat

                            val isBookmarked = bookmarks.any {
                                it.surahNumber == surah.nomor && it.verseNumber == ayat.nomorAyat
                            }

                            VerseItemCard(
                                surah = surah,
                                ayat = ayat,
                                isPlaying = isCurrentlyPlaying,
                                isBookmarked = isBookmarked,
                                settings = settings,
                                onPlayAudio = { onPlayVerseAudio(surah, ayat) },
                                onToggleBookmark = { onToggleBookmark(surah, ayat) },
                                onAddNote = {
                                    noteTargetVerse = ayat
                                    noteInputText = ""
                                },
                                onOpenTafsir = { onNavigateToTafsir(surah.nomor, ayat.nomorAyat) },
                                onMarkRead = {
                                    onMarkLastRead(surah.nomor, surah.namaLatin, ayat.nomorAyat)
                                    Toast.makeText(context, "Ditandai: ${surah.namaLatin} Ayat ${ayat.nomorAyat}", Toast.LENGTH_SHORT).show()
                                },
                                onCopy = {
                                    val clip = ClipData.newPlainText(
                                        "Ayat Al-Qur'an",
                                        "${ayat.teksArab}\n\n${ayat.teksIndonesia}\n(QS. ${surah.namaLatin} [${surah.nomor}]: ${ayat.nomorAyat})"
                                    )
                                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                    Toast.makeText(context, "Ayat berhasil disalin", Toast.LENGTH_SHORT).show()
                                },
                                onShare = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "${ayat.teksArab}\n\n${ayat.teksIndonesia}\n\n— QS. ${surah.namaLatin} [${surah.nomor}]: ${ayat.nomorAyat}\n(DD Qur'an - Teman Ibadah, Setiap Hari)"
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Ayat"))
                                }
                            )
                        }

                        // Previous / Next Surah Navigation at bottom
                        item {
                            SurahBottomNavigationRow(
                                currentSurahNumber = surah.nomor,
                                onNavigateSurah = onNavigateSurah
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }

    // Jump to Verse Dialog
    if (showJumpDialog && surahDetailState is ResourceState.Success) {
        val totalAyat = surahDetailState.data.jumlahAyat
        var targetInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("Lompat ke Ayat") },
            text = {
                Column {
                    Text("Masukkan nomor ayat (1 - $totalAyat):", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = {
                            targetInput = it.filter { char -> char.isDigit() }
                            errorMessage = null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = {
                            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = targetInput.toIntOrNull()
                        if (num != null && num in 1..totalAyat) {
                            showJumpDialog = false
                            coroutineScope.launch {
                                val offset = if (surahNumber == 9) 1 else 2
                                listState.animateScrollToItem((num - 1 + offset).coerceAtLeast(0))
                            }
                        } else {
                            errorMessage = "Nomor ayat harus antara 1 dan $totalAyat"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Lompat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Personal Note Dialog
    noteTargetVerse?.let { ayat ->
        AlertDialog(
            onDismissRequest = { noteTargetVerse = null },
            title = { Text("Catatan Ayat ${ayat.nomorAyat}") },
            text = {
                Column {
                    Text(
                        text = "Tulis refleksi atau tadabbur untuk ayat ini:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Contoh: Ayat ini mengingatkan kita untuk selalu bersabar...") },
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteInputText.isNotBlank() && surahDetailState is ResourceState.Success) {
                            val surah = surahDetailState.data
                            onAddNote(surah.nomor, surah.namaLatin, ayat.nomorAyat, noteInputText.trim())
                            Toast.makeText(context, "Catatan tersimpan", Toast.LENGTH_SHORT).show()
                            noteTargetVerse = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan Catatan")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteTargetVerse = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun SurahHeroBanner(surah: SurahDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ForestDark
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surah.nama,
                fontFamily = AmiriFontFamily,
                fontSize = 32.sp,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = surah.namaLatin,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "(${surah.arti})",
                style = MaterialTheme.typography.bodyMedium,
                color = GoldLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = EmeraldPrimary,
                shape = CircleShape
            ) {
                Text(
                    text = "${surah.tempatTurun} • ${surah.jumlahAyat} Ayat",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun VerseItemCard(
    surah: SurahDetail,
    ayat: AyatItem,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    settings: UserSettings,
    onPlayAudio: () -> Unit,
    onToggleBookmark: () -> Unit,
    onAddNote: () -> Unit,
    onOpenTafsir: () -> Unit,
    onMarkRead: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val borderColor = if (isPlaying) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isPlaying) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .testTag("verse_card_${ayat.nomorAyat}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Verse Header Bar (Number Medallion + Actions)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoldenMedallion(number = ayat.nomorAyat)
                    if (isPlaying) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = EmeraldPrimary,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "Sedang Diputar",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    // Play Verse Audio
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Putar Audio Ayat",
                            tint = if (isPlaying) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Bookmark Toggle
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Simpan Bookmark",
                            tint = if (isBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Personal Note
                    IconButton(
                        onClick = onAddNote,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = "Tambah Catatan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Tafsir Button
                    IconButton(
                        onClick = onOpenTafsir,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoStories,
                            contentDescription = "Tafsir Ayat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Copy
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Salin Ayat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Bagikan Ayat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arabic Text (Right-to-Left, Amiri font)
            Text(
                text = ayat.teksArab,
                fontFamily = AmiriFontFamily,
                fontSize = settings.arabicFontSize.sp,
                lineHeight = (settings.arabicFontSize * 1.8f).sp,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("arabic_text_${ayat.nomorAyat}")
            )

            // Latin Transliteration (if enabled)
            if (settings.showLatin && ayat.teksLatin.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = ayat.teksLatin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmeraldPrimary,
                    fontSize = (settings.translationFontSize - 1).sp,
                    lineHeight = ((settings.translationFontSize - 1) * 1.5f).sp
                )
            }

            // Indonesian Translation (if enabled)
            if (settings.showTranslation && ayat.teksIndonesia.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ayat.teksIndonesia,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = settings.translationFontSize.sp,
                    lineHeight = (settings.translationFontSize * 1.5f).sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Mark as Last Read button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onMarkRead,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tandai Terakhir Dibaca",
                        fontSize = 11.sp,
                        color = EmeraldPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoScrollControlBar(
    isActive: Boolean,
    isPaused: Boolean,
    currentSpeed: Int,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSpeedChanged: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auto_scroll_bar"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isActive) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto-Scroll", fontSize = 12.sp)
                    }
                } else {
                    IconButton(
                        onClick = if (isPaused) onResume else onPause,
                        modifier = Modifier
                            .size(36.dp)
                            .background(GoldLight, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (isPaused) "Lanjutkan" else "Jeda",
                            tint = ForestDark
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onStop,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = "Hentikan",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isPaused) "Dijeda" else "Menggulir...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
            }

            // Speed selector
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Kecepatan:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                listOf(1, 2, 3, 5).forEach { speed ->
                    val isSelected = currentSpeed == speed
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .clickable { onSpeedChanged(speed) }
                    ) {
                        Text(
                            text = "${speed}x",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahBottomNavigationRow(
    currentSurahNumber: Int,
    onNavigateSurah: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentSurahNumber > 1) {
            OutlinedButton(
                onClick = { onNavigateSurah(currentSurahNumber - 1) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("← Surah Sebelumnya")
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (currentSurahNumber < 114) {
            Button(
                onClick = { onNavigateSurah(currentSurahNumber + 1) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Surah Selanjutnya →")
            }
        }
    }
}

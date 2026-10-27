package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.repository.ResourceState
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.MiniAudioPlayer
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.HijriCalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SurahListScreen
import com.example.ui.screens.TafsirScreen
import com.example.ui.theme.DDQuranTheme
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: QuranViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settingsState.collectAsState()
            val isDark = when (settings.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            DDQuranTheme(darkTheme = isDark) {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val audioState by viewModel.audioState.collectAsState()
                val adhanState by viewModel.adhanState.collectAsState()
                val compassState by viewModel.compassState.collectAsState()
                val surahsState by viewModel.surahsState.collectAsState()
                val surahDetailState by viewModel.surahDetailState.collectAsState()
                val tafsirDetailState by viewModel.tafsirDetailState.collectAsState()
                val lastRead by viewModel.lastReadState.collectAsState()
                val bookmarks by viewModel.bookmarksList.collectAsState()
                val notes by viewModel.notesList.collectAsState()
                val history by viewModel.historyList.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                val isSearching by viewModel.isSearching.collectAsState()
                val verseSearchResults by viewModel.verseSearchResults.collectAsState()
                val prayerSchedule by viewModel.prayerSchedule.collectAsState()
                val prayerCalendar by viewModel.prayerCalendar.collectAsState()
                val todayHijri by viewModel.todayHijriDate.collectAsState()
                val hijriMonthCalendar by viewModel.hijriMonthCalendar.collectAsState()
                val hijriDaysGrid by viewModel.hijriDaysGrid.collectAsState()

                // Handle system back button
                BackHandler(enabled = currentScreen !is Screen.Home) {
                    viewModel.navigateBack()
                }

                val showBottomNav = currentScreen is Screen.Home ||
                        currentScreen is Screen.SurahList ||
                        currentScreen is Screen.PrayerTimes ||
                        currentScreen is Screen.Qibla ||
                        currentScreen is Screen.HijriCalendar ||
                        currentScreen is Screen.Search ||
                        currentScreen is Screen.Bookmarks ||
                        currentScreen is Screen.Settings

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Mini Quran Audio Player appears docked above bottom bar when playing
                            MiniAudioPlayer(
                                state = audioState,
                                onTogglePlayPause = { viewModel.togglePlayPauseAudio() },
                                onNext = { viewModel.nextAudioVerse() },
                                onPrev = { viewModel.prevAudioVerse() },
                                onClose = { viewModel.stopAudio() }
                            )

                            if (showBottomNav) {
                                AppBottomNavigation(
                                    currentScreen = currentScreen,
                                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else 0.dp,
                                top = if (currentScreen is Screen.Reader || currentScreen is Screen.Tafsir) {
                                    0.dp // Managed internally by TopAppBar in those screens
                                } else {
                                    innerPadding.calculateTopPadding()
                                }
                            )
                    ) {
                        when (val screen = currentScreen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    lastRead = lastRead,
                                    settings = settings,
                                    history = history,
                                    prayerSchedule = prayerSchedule,
                                    todayHijri = todayHijri,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onIncrementAyatProgress = { viewModel.incrementDailyAyatCount() }
                                )
                            }
                            is Screen.SurahList -> {
                                SurahListScreen(
                                    surahsState = surahsState,
                                    onSelectSurah = { nomor ->
                                        viewModel.navigateTo(Screen.Reader(nomor))
                                    },
                                    onPlaySurahAudio = { surahItem ->
                                        viewModel.loadSurahDetail(surahItem.nomor)
                                        val detail = (surahDetailState as? ResourceState.Success)?.data
                                        if (detail != null && detail.nomor == surahItem.nomor) {
                                            viewModel.playFullSurahAudio(detail)
                                        } else {
                                            viewModel.navigateTo(Screen.Reader(surahItem.nomor))
                                        }
                                    },
                                    onRetry = { viewModel.loadSurahs(forceRefresh = true) }
                                )
                            }
                            is Screen.Reader -> {
                                ReaderScreen(
                                    surahNumber = screen.surahNumber,
                                    targetVerseNumber = screen.targetVerseNumber,
                                    surahDetailState = surahDetailState,
                                    audioState = audioState,
                                    settings = settings,
                                    bookmarks = bookmarks,
                                    onLoadSurah = { no -> viewModel.loadSurahDetail(no) },
                                    onBack = { viewModel.navigateBack() },
                                    onNavigateSurah = { no ->
                                        viewModel.navigateTo(Screen.Reader(no))
                                    },
                                    onNavigateToTafsir = { surahNo, verseNo ->
                                        viewModel.navigateTo(Screen.Tafsir(surahNo, verseNo))
                                    },
                                    onPlayVerseAudio = { surahDetail, ayat ->
                                        viewModel.playVerseAudio(surahDetail.nomor, surahDetail.namaLatin, ayat, surahDetail.ayat)
                                    },
                                    onToggleBookmark = { surahDetail, ayat ->
                                        viewModel.toggleBookmark(
                                            surahNumber = surahDetail.nomor,
                                            surahName = surahDetail.namaLatin,
                                            verseNumber = ayat.nomorAyat,
                                            arabicText = ayat.teksArab,
                                            translationText = ayat.teksIndonesia
                                        )
                                    },
                                    onAddNote = { surahNo, surahName, verseNo, content ->
                                        viewModel.addNote(surahNo, surahName, verseNo, content)
                                    },
                                    onMarkLastRead = { surahNo, surahName, verseNo ->
                                        viewModel.markLastRead(surahNo, surahName, verseNo)
                                    }
                                )
                            }
                            is Screen.Tafsir -> {
                                TafsirScreen(
                                    surahNumber = screen.surahNumber,
                                    targetVerseNumber = screen.targetVerseNumber,
                                    tafsirState = tafsirDetailState,
                                    onLoadTafsir = { no -> viewModel.loadTafsirDetail(no) },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }
                            is Screen.PrayerTimes -> {
                                PrayerTimesScreen(
                                    prayerSchedule = prayerSchedule,
                                    calendar = prayerCalendar,
                                    settings = settings,
                                    adhanState = adhanState,
                                    onNavigateDate = { offset -> viewModel.navigatePrayerDate(offset) },
                                    onResetToday = { viewModel.resetPrayerDateToToday() },
                                    onSelectCity = { city -> viewModel.setLocationFromCity(city) },
                                    onSelectMethod = { methodId -> viewModel.setCalculationMethod(methodId) },
                                    onTogglePrayerNotification = { name, enabled ->
                                        viewModel.togglePrayerNotification(name, enabled)
                                    },
                                    onTestAdhanSound = { viewModel.testAdhanSound() },
                                    onStopAdhanSound = { viewModel.stopAdhanSound() }
                                )
                            }
                            is Screen.Qibla -> {
                                QiblaScreen(
                                    compassState = compassState,
                                    settings = settings,
                                    qiblaBearing = viewModel.qiblaBearing,
                                    distanceKm = viewModel.distanceToKaabaKm,
                                    onStartCompass = { viewModel.startCompass() },
                                    onStopCompass = { viewModel.stopCompass() },
                                    onSelectCity = { city -> viewModel.setLocationFromCity(city) },
                                    onLocationUpdated = { name, lat, lng, tz ->
                                        viewModel.setLocationFromGps(name, lat, lng, tz)
                                    }
                                )
                            }
                            is Screen.HijriCalendar -> {
                                HijriCalendarScreen(
                                    todayHijri = todayHijri,
                                    currentCalendar = hijriMonthCalendar,
                                    daysGrid = hijriDaysGrid,
                                    adjustmentDays = settings.hijriAdjustmentDays,
                                    onNavigateMonth = { offset -> viewModel.navigateHijriMonth(offset) },
                                    onResetCurrentMonth = { viewModel.resetHijriMonthToCurrent() },
                                    onUpdateAdjustment = { adj -> viewModel.setHijriAdjustment(adj) }
                                )
                            }
                            is Screen.Bookmarks -> {
                                BookmarksScreen(
                                    bookmarks = bookmarks,
                                    notes = notes,
                                    history = history,
                                    onSelectVerse = { surahNo, verseNo ->
                                        viewModel.navigateTo(Screen.Reader(surahNo, verseNo))
                                    },
                                    onDeleteBookmark = { bookmark ->
                                        viewModel.removeBookmark(bookmark)
                                    },
                                    onDeleteNote = { noteId ->
                                        viewModel.deleteNote(noteId)
                                    }
                                )
                            }
                            is Screen.Search -> {
                                val surahsList = (surahsState as? ResourceState.Success)?.data ?: emptyList()
                                SearchScreen(
                                    query = searchQuery,
                                    isSearching = isSearching,
                                    surahs = surahsList,
                                    verseResults = verseSearchResults,
                                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                    onSelectSurah = { surahNo ->
                                        viewModel.navigateTo(Screen.Reader(surahNo))
                                    },
                                    onSelectVerse = { surahNo, verseNo ->
                                        viewModel.navigateTo(Screen.Reader(surahNo, verseNo))
                                    }
                                )
                            }
                            is Screen.Settings -> {
                                SettingsScreen(
                                    settings = settings,
                                    onUpdateArabicFontSize = { viewModel.setArabicFontSize(it) },
                                    onUpdateTranslationFontSize = { viewModel.setTranslationFontSize(it) },
                                    onUpdateShowLatin = { viewModel.setShowLatin(it) },
                                    onUpdateShowTranslation = { viewModel.setShowTranslation(it) },
                                    onUpdateSelectedQari = { viewModel.setSelectedQari(it) },
                                    onUpdateDailyTarget = { viewModel.setDailyTarget(it) },
                                    onUpdateAutoScrollSpeed = { viewModel.setAutoScrollSpeed(it) },
                                    onUpdateThemeMode = { viewModel.setThemeMode(it) },
                                    onResetSettings = { viewModel.resetSettings() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

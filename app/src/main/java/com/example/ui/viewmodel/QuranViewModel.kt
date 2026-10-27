package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackState
import com.example.audio.AudioPlayerManager
import com.example.data.api.QuranNetworkClient
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.LastReadPosition
import com.example.data.local.ReadingHistoryEntity
import com.example.data.local.ReadingNoteEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserSettings
import com.example.data.model.AVAILABLE_CALCULATION_METHODS
import com.example.data.model.AVAILABLE_QARIS
import com.example.data.model.AyatItem
import com.example.data.model.CityLocation
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.HijriCalendarDay
import com.example.data.model.HijriDate
import com.example.data.model.PRESET_CITIES
import com.example.data.model.SurahDetail
import com.example.data.model.SurahItem
import com.example.data.model.TafsirDetail
import com.example.data.repository.QuranRepository
import com.example.data.repository.ResourceState
import com.example.data.repository.SearchResultVerse
import com.example.service.AdhanPlaybackState
import com.example.service.AdhanPlayer
import com.example.service.CompassSensorManager
import com.example.service.CompassSensorState
import com.example.service.HijriCalendarHelper
import com.example.service.PrayerTimesCalculator
import com.example.service.QiblaCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

sealed class Screen {
    object Home : Screen()
    object SurahList : Screen()
    data class Reader(val surahNumber: Int, val targetVerseNumber: Int? = null) : Screen()
    data class Tafsir(val surahNumber: Int, val targetVerseNumber: Int? = null) : Screen()
    object Bookmarks : Screen()
    object Search : Screen()
    object Settings : Screen()
    object Qibla : Screen()
    object PrayerTimes : Screen()
    object HijriCalendar : Screen()
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = QuranRepository(QuranNetworkClient.apiService, database)
    val userPreferences = UserPreferences(application)
    val audioPlayerManager = AudioPlayerManager(application)
    val compassSensorManager = CompassSensorManager(application)
    val adhanPlayer = AdhanPlayer(application)

    // Navigation Stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val currentScreen: StateFlow<Screen> = MutableStateFlow<Screen>(Screen.Home).apply {
        viewModelScope.launch {
            _screenStack.collect { stack ->
                value = stack.lastOrNull() ?: Screen.Home
            }
        }
    }

    fun navigateTo(screen: Screen) {
        val current = _screenStack.value
        if (screen is Screen.Home) {
            _screenStack.value = listOf(Screen.Home)
        } else {
            if (current.lastOrNull() != screen) {
                _screenStack.value = current + screen
            }
        }
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        return if (current.size > 1) {
            _screenStack.value = current.dropLast(1)
            true
        } else {
            false
        }
    }

    // Settings & Last Read flows
    val settingsState: StateFlow<UserSettings> = userPreferences.settingsFlow
    val lastReadState: StateFlow<LastReadPosition> = userPreferences.lastReadFlow

    // Bookmarks, Notes, History
    val bookmarksList: StateFlow<List<BookmarkEntity>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notesList: StateFlow<List<ReadingNoteEntity>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val historyList: StateFlow<List<ReadingHistoryEntity>> = repository.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Audio Playback State (Quran)
    val audioState: StateFlow<AudioPlaybackState> = audioPlayerManager.playbackState

    // Adhan Playback State
    val adhanState: StateFlow<AdhanPlaybackState> = adhanPlayer.playbackState

    // Compass Sensor State
    val compassState: StateFlow<CompassSensorState> = compassSensorManager.sensorState

    // Surahs State
    private val _surahsState = MutableStateFlow<ResourceState<List<SurahItem>>>(ResourceState.Idle)
    val surahsState: StateFlow<ResourceState<List<SurahItem>>> = _surahsState.asStateFlow()

    // Surah Reader Detail State
    private val _surahDetailState = MutableStateFlow<ResourceState<SurahDetail>>(ResourceState.Idle)
    val surahDetailState: StateFlow<ResourceState<SurahDetail>> = _surahDetailState.asStateFlow()

    // Tafsir Detail State
    private val _tafsirDetailState = MutableStateFlow<ResourceState<TafsirDetail>>(ResourceState.Idle)
    val tafsirDetailState: StateFlow<ResourceState<TafsirDetail>> = _tafsirDetailState.asStateFlow()

    // Global Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _verseSearchResults = MutableStateFlow<List<SearchResultVerse>>(emptyList())
    val verseSearchResults: StateFlow<List<SearchResultVerse>> = _verseSearchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    // --- Prayer Times State ---
    private val _prayerCalendar = MutableStateFlow(Calendar.getInstance())
    val prayerCalendar: StateFlow<Calendar> = _prayerCalendar.asStateFlow()

    private val _prayerSchedule = MutableStateFlow<DailyPrayerSchedule?>(null)
    val prayerSchedule: StateFlow<DailyPrayerSchedule?> = _prayerSchedule.asStateFlow()

    // --- Hijri Calendar State ---
    private val _hijriMonthCalendar = MutableStateFlow(Calendar.getInstance())
    val hijriMonthCalendar: StateFlow<Calendar> = _hijriMonthCalendar.asStateFlow()

    private val _hijriDaysGrid = MutableStateFlow<List<HijriCalendarDay>>(emptyList())
    val hijriDaysGrid: StateFlow<List<HijriCalendarDay>> = _hijriDaysGrid.asStateFlow()

    private val _todayHijriDate = MutableStateFlow(
        HijriCalendarHelper.getHijriDate(Calendar.getInstance())
    )
    val todayHijriDate: StateFlow<HijriDate> = _todayHijriDate.asStateFlow()

    // --- Qibla Calculation State ---
    val qiblaBearing: Double
        get() = QiblaCalculator.calculateQiblaBearing(
            settingsState.value.latitude,
            settingsState.value.longitude
        )

    val distanceToKaabaKm: Double
        get() = QiblaCalculator.calculateDistanceToKaabaKm(
            settingsState.value.latitude,
            settingsState.value.longitude
        )

    init {
        loadSurahs()
        refreshPrayerSchedule()
        refreshHijriCalendar()
        startPrayerCountdownTicker()
    }

    private fun startPrayerCountdownTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                updatePrayerCountdown()
            }
        }
    }

    private fun updatePrayerCountdown() {
        val current = _prayerSchedule.value ?: return
        val now = System.currentTimeMillis()
        val all = current.getList()

        var nextPt = all.firstOrNull { it.timestamp > now }
        val countdown = if (nextPt != null) {
            (nextPt.timestamp - now).coerceAtLeast(0L)
        } else {
            val calTomorrow = _prayerCalendar.value.clone() as Calendar
            calTomorrow.add(Calendar.DAY_OF_YEAR, 1)
            val subuhNext = current.subuh.timestamp + 86400000L
            (subuhNext - now).coerceAtLeast(0L)
        }

        _prayerSchedule.value = current.copy(
            countdownMillis = countdown,
            nextPrayer = nextPt
        )
    }

    fun refreshPrayerSchedule() {
        val settings = settingsState.value
        val cal = _prayerCalendar.value.clone() as Calendar
        cal.timeZone = TimeZone.getTimeZone(settings.timeZoneId)

        val method = AVAILABLE_CALCULATION_METHODS.find { it.id == settings.calculationMethodId }
            ?: AVAILABLE_CALCULATION_METHODS[0]

        val schedule = PrayerTimesCalculator.calculateSchedule(
            calendar = cal,
            latitude = settings.latitude,
            longitude = settings.longitude,
            cityName = settings.cityName,
            method = method
        )
        _prayerSchedule.value = schedule
    }

    fun navigatePrayerDate(daysOffset: Int) {
        val cal = _prayerCalendar.value.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        _prayerCalendar.value = cal
        refreshPrayerSchedule()
    }

    fun resetPrayerDateToToday() {
        _prayerCalendar.value = Calendar.getInstance()
        refreshPrayerSchedule()
    }

    fun refreshHijriCalendar() {
        val cal = _hijriMonthCalendar.value
        val adj = settingsState.value.hijriAdjustmentDays
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        _hijriDaysGrid.value = HijriCalendarHelper.getMonthGrid(year, month, adj)
        _todayHijriDate.value = HijriCalendarHelper.getHijriDate(Calendar.getInstance(), adj)
    }

    fun navigateHijriMonth(monthOffset: Int) {
        val cal = _hijriMonthCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, monthOffset)
        _hijriMonthCalendar.value = cal
        refreshHijriCalendar()
    }

    fun resetHijriMonthToCurrent() {
        _hijriMonthCalendar.value = Calendar.getInstance()
        refreshHijriCalendar()
    }

    fun setLocationFromCity(city: CityLocation) {
        userPreferences.updateLocation(
            cityName = city.name,
            latitude = city.latitude,
            longitude = city.longitude,
            timeZoneId = city.timeZoneId
        )
        refreshPrayerSchedule()
    }

    fun setLocationFromGps(cityName: String, lat: Double, lng: Double, timeZoneId: String) {
        userPreferences.updateLocation(
            cityName = cityName,
            latitude = lat,
            longitude = lng,
            timeZoneId = timeZoneId
        )
        refreshPrayerSchedule()
    }

    fun setCalculationMethod(methodId: String) {
        userPreferences.updateCalculationMethod(methodId)
        refreshPrayerSchedule()
    }

    fun setHijriAdjustment(adjustment: Int) {
        userPreferences.updateHijriAdjustment(adjustment)
        refreshHijriCalendar()
    }

    fun togglePrayerNotification(prayerName: String, enabled: Boolean) {
        userPreferences.updatePrayerNotification(prayerName, enabled)
    }

    fun togglePlayAdhanSound(enabled: Boolean) {
        userPreferences.updatePlayAdhanSound(enabled)
    }

    fun testAdhanSound() {
        adhanPlayer.testAdhanSound()
    }

    fun stopAdhanSound() {
        adhanPlayer.stop()
    }

    fun startCompass() {
        compassSensorManager.startListening()
    }

    fun stopCompass() {
        compassSensorManager.stopListening()
    }

    // --- Quran Core Methods ---
    fun loadSurahs(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _surahsState.value = ResourceState.Loading
            repository.getSurahs(forceRefresh)
                .onSuccess { list ->
                    _surahsState.value = ResourceState.Success(list)
                }
                .onFailure { error ->
                    _surahsState.value = ResourceState.Error(error.localizedMessage ?: "Gagal memuat data surah")
                }
        }
    }

    fun loadSurahDetail(surahNumber: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _surahDetailState.value = ResourceState.Loading
            repository.getSurahDetail(surahNumber, forceRefresh)
                .onSuccess { detail ->
                    _surahDetailState.value = ResourceState.Success(detail)
                    repository.getTafsirDetail(surahNumber)
                }
                .onFailure { error ->
                    _surahDetailState.value = ResourceState.Error(error.localizedMessage ?: "Gagal memuat ayat")
                }
        }
    }

    fun loadTafsirDetail(surahNumber: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _tafsirDetailState.value = ResourceState.Loading
            repository.getTafsirDetail(surahNumber, forceRefresh)
                .onSuccess { detail ->
                    _tafsirDetailState.value = ResourceState.Success(detail)
                }
                .onFailure { error ->
                    _tafsirDetailState.value = ResourceState.Error(error.localizedMessage ?: "Gagal memuat tafsir")
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _verseSearchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            _isSearching.value = true
            val results = repository.searchVerses(query)
            _verseSearchResults.value = results
            _isSearching.value = false
        }
    }

    // Audio Playback
    fun playVerseAudio(surahNumber: Int, surahName: String, ayat: AyatItem, allAyat: List<AyatItem>) {
        val qariId = settingsState.value.selectedQariId
        val qariName = AVAILABLE_QARIS.find { it.id == qariId }?.name ?: "Qari"
        audioPlayerManager.playVerse(surahNumber, surahName, ayat, qariId, qariName, allAyat)
    }

    fun playFullSurahAudio(surah: SurahDetail) {
        val qariId = settingsState.value.selectedQariId
        val qariName = AVAILABLE_QARIS.find { it.id == qariId }?.name ?: "Qari"
        audioPlayerManager.playFullSurah(surah, qariId, qariName)
    }

    fun togglePlayPauseAudio() {
        audioPlayerManager.togglePlayPause()
    }

    fun stopAudio() {
        audioPlayerManager.stop()
    }

    fun nextAudioVerse() {
        audioPlayerManager.nextVerse()
    }

    fun prevAudioVerse() {
        audioPlayerManager.previousVerse()
    }

    // Bookmarks
    fun toggleBookmark(
        surahNumber: Int,
        surahName: String,
        verseNumber: Int,
        arabicText: String,
        translationText: String
    ) {
        viewModelScope.launch {
            repository.toggleBookmark(surahNumber, surahName, verseNumber, arabicText, translationText)
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
        }
    }

    // Notes
    fun addNote(surahNumber: Int, surahName: String, verseNumber: Int, content: String) {
        viewModelScope.launch {
            repository.addNote(surahNumber, surahName, verseNumber, content)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // Last Read & Progress
    fun markLastRead(surahNumber: Int, surahName: String, verseNumber: Int) {
        userPreferences.saveLastRead(surahNumber, surahName, verseNumber)
        viewModelScope.launch {
            repository.addHistory(surahNumber, surahName, verseNumber)
        }
    }

    fun incrementDailyAyatCount() {
        userPreferences.incrementDailyAyatCount(1)
    }

    // Settings Updaters
    fun setArabicFontSize(size: Float) = userPreferences.updateArabicFontSize(size)
    fun setTranslationFontSize(size: Float) = userPreferences.updateTranslationFontSize(size)
    fun setShowLatin(show: Boolean) = userPreferences.updateShowLatin(show)
    fun setShowTranslation(show: Boolean) = userPreferences.updateShowTranslation(show)
    fun setAutoScrollSpeed(speed: Int) = userPreferences.updateAutoScrollSpeed(speed)
    fun setSelectedQari(qariId: String) = userPreferences.updateSelectedQari(qariId)
    fun setDailyTarget(target: Int) = userPreferences.updateDailyTarget(target)
    fun setThemeMode(mode: String) = userPreferences.updateThemeMode(mode)
    fun resetSettings() = userPreferences.resetToDefaults()

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
        adhanPlayer.release()
        compassSensorManager.stopListening()
    }
}

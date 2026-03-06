package com.scripturewidgets.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scripturewidgets.data.PreferencesRepository
import com.scripturewidgets.domain.model.*
import com.scripturewidgets.domain.repository.VerseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════
// TodayViewModel
// ═══════════════════════════════════════════════════════════════
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: VerseRepository,
    private val preferences: PreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    val widgetConfig: StateFlow<WidgetConfig> = preferences.widgetConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetConfig())

    val userName: StateFlow<String> = preferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val streakDays: StateFlow<Int> = preferences.streakDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalVersesRead: StateFlow<Int> = preferences.totalVersesRead
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val randomizeOnOpen: StateFlow<Boolean> = preferences.randomizeOnOpen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userProfile: StateFlow<UserProfile> = preferences.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    init {
        viewModelScope.launch {
            preferences.updateStreak()
            val randomize = preferences.randomizeOnOpen.first()
            if (randomize) loadRandomVerse() else loadDailyVerse()
        }
    }

    fun loadDailyVerse() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val verse = repository.getDailyVerse()
                if (verse != null) preferences.incrementVersesRead()
                _uiState.update { it.copy(isLoading = false, dailyVerse = verse) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load verse") }
            }
        }
    }

    fun loadRandomVerse() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val verse = repository.getRandomVerse()
                if (verse != null) preferences.incrementVersesRead()
                _uiState.update { it.copy(isLoading = false, dailyVerse = verse) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load verse") }
            }
        }
    }

    fun toggleFavorite(verse: BibleVerse) {
        viewModelScope.launch {
            val isFav = repository.isFavorite(verse.id).first()
            if (isFav) repository.removeFavorite(verse.id) else repository.addFavorite(verse)
        }
    }

    fun isFavorite(verseId: String): Flow<Boolean> = repository.isFavorite(verseId)

    fun shareVerse(verse: BibleVerse) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, verse.shareText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Share Verse").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}

data class TodayUiState(
    val isLoading: Boolean    = false,
    val dailyVerse: BibleVerse? = null,
    val error: String?        = null
)

// ═══════════════════════════════════════════════════════════════
// BrowseViewModel
// ═══════════════════════════════════════════════════════════════
@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: VerseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery    = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(VerseCategory.ALL)

    val searchQuery: StateFlow<String>           = _searchQuery.asStateFlow()
    val selectedCategory: StateFlow<VerseCategory> = _selectedCategory.asStateFlow()

    val verses: StateFlow<List<BibleVerse>> = combine(_searchQuery, _selectedCategory) { q, cat -> Pair(q, cat) }
        .debounce(300)
        .flatMapLatest { (q, cat) ->
            if (q.isBlank()) repository.getVersesByCategory(cat)
            else repository.searchVerses(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearch(query: String) { _searchQuery.value = query }
    fun selectCategory(cat: VerseCategory) { _selectedCategory.value = cat }

    fun toggleFavorite(verse: BibleVerse) {
        viewModelScope.launch {
            val isFav = repository.isFavorite(verse.id).first()
            if (isFav) repository.removeFavorite(verse.id) else repository.addFavorite(verse)
        }
    }

    fun isFavorite(verseId: String): Flow<Boolean> = repository.isFavorite(verseId)

    fun shareVerse(verse: BibleVerse) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, verse.shareText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Share Verse").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}

// ═══════════════════════════════════════════════════════════════
// FavoritesViewModel
// ═══════════════════════════════════════════════════════════════
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: VerseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val favorites: StateFlow<List<BibleVerse>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(verseId: String) {
        viewModelScope.launch { repository.removeFavorite(verseId) }
    }

    fun shareVerse(verse: BibleVerse) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, verse.shareText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Share Verse").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}

// ═══════════════════════════════════════════════════════════════
// SettingsViewModel
// ═══════════════════════════════════════════════════════════════
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferencesRepository
) : ViewModel() {

    val widgetConfig: StateFlow<WidgetConfig> = preferences.widgetConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetConfig())

    val hasPremium: StateFlow<Boolean> = preferences.hasPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notificationsEnabled: StateFlow<Boolean> = preferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationFrequency: StateFlow<NotificationFrequency> = preferences.notificationFrequency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationFrequency.ONCE_DAILY)

    val notificationHour: StateFlow<Int> = preferences.notificationHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    val notificationMinute: StateFlow<Int> = preferences.notificationMinute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notificationCategory: StateFlow<VerseCategory> = preferences.notificationCategory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VerseCategory.ALL)

    val notificationSchedules: StateFlow<List<NotificationSchedule>> = preferences.notificationSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userName: StateFlow<String> = preferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val streakDays: StateFlow<Int> = preferences.streakDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalVersesRead: StateFlow<Int> = preferences.totalVersesRead
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val randomizeOnOpen: StateFlow<Boolean> = preferences.randomizeOnOpen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userProfile: StateFlow<UserProfile> = preferences.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val appThemeDark: StateFlow<Boolean> = preferences.appThemeDark
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val defaultTranslation: StateFlow<BibleTranslation> = preferences.defaultTranslation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BibleTranslation.KJV)

    // ── Widget config updates ─────────────────────────────────────
    private fun updateConfig(block: WidgetConfig.() -> WidgetConfig) {
        viewModelScope.launch { preferences.saveWidgetConfig(widgetConfig.value.block()) }
    }

    fun updateTheme(theme: WidgetTheme)               = updateConfig { copy(theme = theme) }
    fun updateFontStyle(style: WidgetFontStyle)        = updateConfig { copy(fontStyle = style) }
    fun updateFontSize(size: Float)                    = updateConfig { copy(fontSize = size) }
    fun updateTranslation(t: BibleTranslation)         = updateConfig { copy(translation = t) }
    fun updateContentType(type: WidgetContentType)     = updateConfig { copy(contentType = type) }
    fun updateCategory(cat: VerseCategory)             = updateConfig { copy(category = cat) }
    fun toggleShowReference(v: Boolean)                = updateConfig { copy(showReference = v) }
    fun toggleShowTranslation(v: Boolean)              = updateConfig { copy(showTranslation = v) }
    fun updateTextAlign(align: WidgetTextAlign)        = updateConfig { copy(textAlign = align) }
    fun updateBackground(bg: WidgetBackground)         = updateConfig { copy(background = bg) }
    fun updateCustomBgColor(hex: Long)                 = updateConfig { copy(customBgColorHex = hex) }
    fun updateCustomTextColor(hex: Long)               = updateConfig { copy(customTextColorHex = hex) }
    fun toggleShowVerseNumber(v: Boolean)              = updateConfig { copy(showVerseNumber = v) }
    fun toggleCompactMode(v: Boolean)                  = updateConfig { copy(compactMode = v) }

    // ── Notification updates ──────────────────────────────────────
    fun updateNotificationSettings(
        enabled: Boolean,
        hour: Int,
        minute: Int = notificationMinute.value,
        frequency: NotificationFrequency = notificationFrequency.value,
        category: VerseCategory = notificationCategory.value
    ) {
        viewModelScope.launch {
            preferences.saveNotificationSettings(enabled, hour, minute, frequency, category)
        }
    }

    fun addNotificationSchedule(schedule: NotificationSchedule) {
        viewModelScope.launch {
            val updated = notificationSchedules.value + schedule
            preferences.saveNotificationSchedules(updated)
        }
    }

    fun removeNotificationSchedule(id: Int) {
        viewModelScope.launch {
            val updated = notificationSchedules.value.filterNot { it.id == id }
            preferences.saveNotificationSchedules(updated)
        }
    }

    fun toggleSchedule(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            val updated = notificationSchedules.value.map { if (it.id == id) it.copy(enabled = enabled) else it }
            preferences.saveNotificationSchedules(updated)
        }
    }

    // ── User & App preferences ────────────────────────────────────
    fun saveUserName(name: String) = viewModelScope.launch { preferences.saveUserName(name) }
    fun setRandomizeOnOpen(v: Boolean) = viewModelScope.launch { preferences.setRandomizeOnOpen(v) }
    fun setAppThemeDark(v: Boolean) = viewModelScope.launch { preferences.setAppThemeDark(v) }
    fun setDefaultTranslation(t: BibleTranslation) = viewModelScope.launch { preferences.setDefaultTranslation(t) }
    fun setPremium(value: Boolean) = viewModelScope.launch { preferences.setPremium(value) }
}

// data/PreferencesDataStore.kt
package com.scripturewidgets.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.scripturewidgets.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scripture_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val WIDGET_THEME            = stringPreferencesKey("widget_theme")
        val WIDGET_FONT_STYLE       = stringPreferencesKey("widget_font_style")
        val WIDGET_FONT_SIZE        = floatPreferencesKey("widget_font_size")
        val WIDGET_TRANSLATION      = stringPreferencesKey("widget_translation")
        val WIDGET_CONTENT_TYPE     = stringPreferencesKey("widget_content_type")
        val WIDGET_CATEGORY         = stringPreferencesKey("widget_category")
        val SHOW_REFERENCE          = booleanPreferencesKey("show_reference")
        val SHOW_TRANSLATION        = booleanPreferencesKey("show_translation")
        val TEXT_ALIGN              = stringPreferencesKey("text_align")
        val WIDGET_BACKGROUND       = stringPreferencesKey("widget_background")
        val CUSTOM_BG_COLOR         = longPreferencesKey("custom_bg_color")
        val CUSTOM_TEXT_COLOR       = longPreferencesKey("custom_text_color")
        val SHOW_VERSE_NUMBER       = booleanPreferencesKey("show_verse_number")
        val COMPACT_MODE            = booleanPreferencesKey("compact_mode")
        val NOTIFICATIONS_ENABLED   = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_FREQUENCY  = stringPreferencesKey("notification_frequency")
        val NOTIFICATION_HOUR       = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE     = intPreferencesKey("notification_minute")
        val NOTIFICATION_CATEGORY   = stringPreferencesKey("notification_category")
        val NOTIFICATION_SCHEDULES  = stringPreferencesKey("notification_schedules")
        val USER_NAME               = stringPreferencesKey("user_name")
        val STREAK_DAYS             = intPreferencesKey("streak_days")
        val TOTAL_VERSES_READ       = intPreferencesKey("total_verses_read")
        val LAST_OPEN_DATE          = longPreferencesKey("last_open_date")
        val HAS_PREMIUM             = booleanPreferencesKey("has_premium")
        val HAS_SEEN_ONBOARDING     = booleanPreferencesKey("has_seen_onboarding")
        val DEFAULT_TRANSLATION     = stringPreferencesKey("default_translation")
        val APP_THEME_DARK          = booleanPreferencesKey("app_theme_dark")
        val RANDOMIZE_ON_OPEN       = booleanPreferencesKey("randomize_on_open")
        // Profile
        val AGE_GROUP               = stringPreferencesKey("age_group")
        val FAITH_JOURNEY           = stringPreferencesKey("faith_journey")
        val LIFE_SEASON             = stringPreferencesKey("life_season")
        val PRIMARY_GOAL            = stringPreferencesKey("primary_goal")
        val SECONDARY_GOALS         = stringPreferencesKey("secondary_goals")
        val TRADITION               = stringPreferencesKey("tradition")
        val READING_PACE            = stringPreferencesKey("reading_pace")
        val ONBOARDING_DONE         = booleanPreferencesKey("onboarding_v2_done")
    }

    private fun <T> safe(value: String?, block: (String) -> T): T? =
        value?.let { runCatching { block(it) }.getOrNull() }

    // ── Widget Config ─────────────────────────────────────────────
    val widgetConfig: Flow<WidgetConfig> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            WidgetConfig(
                theme          = safe(p[Keys.WIDGET_THEME]) { WidgetTheme.valueOf(it) } ?: WidgetTheme.SUNRISE,
                fontStyle      = safe(p[Keys.WIDGET_FONT_STYLE]) { WidgetFontStyle.valueOf(it) } ?: WidgetFontStyle.SERIF,
                fontSize       = p[Keys.WIDGET_FONT_SIZE] ?: 14f,
                translation    = BibleTranslation.fromAbbreviation(p[Keys.WIDGET_TRANSLATION] ?: "KJV"),
                contentType    = safe(p[Keys.WIDGET_CONTENT_TYPE]) { WidgetContentType.valueOf(it) } ?: WidgetContentType.DAILY_VERSE,
                category       = safe(p[Keys.WIDGET_CATEGORY]) { VerseCategory.valueOf(it) } ?: VerseCategory.ALL,
                showReference  = p[Keys.SHOW_REFERENCE] ?: true,
                showTranslation = p[Keys.SHOW_TRANSLATION] ?: true,
                textAlign      = safe(p[Keys.TEXT_ALIGN]) { WidgetTextAlign.valueOf(it) } ?: WidgetTextAlign.CENTER,
                background     = safe(p[Keys.WIDGET_BACKGROUND]) { WidgetBackground.valueOf(it) } ?: WidgetBackground.GRADIENT,
                customBgColorHex   = p[Keys.CUSTOM_BG_COLOR] ?: 0xFF1A237E,
                customTextColorHex = p[Keys.CUSTOM_TEXT_COLOR] ?: 0xFFFFFFFF,
                showVerseNumber = p[Keys.SHOW_VERSE_NUMBER] ?: true,
                compactMode    = p[Keys.COMPACT_MODE] ?: false
            )
        }

    suspend fun saveWidgetConfig(config: WidgetConfig) {
        context.dataStore.edit { p ->
            p[Keys.WIDGET_THEME]          = config.theme.name
            p[Keys.WIDGET_FONT_STYLE]     = config.fontStyle.name
            p[Keys.WIDGET_FONT_SIZE]      = config.fontSize
            p[Keys.WIDGET_TRANSLATION]    = config.translation.abbreviation
            p[Keys.WIDGET_CONTENT_TYPE]   = config.contentType.name
            p[Keys.WIDGET_CATEGORY]       = config.category.name
            p[Keys.SHOW_REFERENCE]        = config.showReference
            p[Keys.SHOW_TRANSLATION]      = config.showTranslation
            p[Keys.TEXT_ALIGN]            = config.textAlign.name
            p[Keys.WIDGET_BACKGROUND]     = config.background.name
            p[Keys.CUSTOM_BG_COLOR]       = config.customBgColorHex
            p[Keys.CUSTOM_TEXT_COLOR]     = config.customTextColorHex
            p[Keys.SHOW_VERSE_NUMBER]     = config.showVerseNumber
            p[Keys.COMPACT_MODE]          = config.compactMode
        }
    }

    // ── Notifications ─────────────────────────────────────────────
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    val notificationFrequency: Flow<NotificationFrequency> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { safe(it[Keys.NOTIFICATION_FREQUENCY]) { s -> NotificationFrequency.valueOf(s) } ?: NotificationFrequency.ONCE_DAILY }

    val notificationHour: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }.map { it[Keys.NOTIFICATION_HOUR] ?: 8 }

    val notificationMinute: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }.map { it[Keys.NOTIFICATION_MINUTE] ?: 0 }

    val notificationCategory: Flow<VerseCategory> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { safe(it[Keys.NOTIFICATION_CATEGORY]) { s -> VerseCategory.valueOf(s) } ?: VerseCategory.ALL }

    val notificationSchedules: Flow<List<NotificationSchedule>> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val raw = prefs[Keys.NOTIFICATION_SCHEDULES] ?: ""
            if (raw.isEmpty()) {
                listOf(NotificationSchedule(0, 8, 0, true, VerseCategory.ALL, "Morning"))
            } else {
                raw.split("|").mapIndexedNotNull { idx, entry ->
                    val parts = entry.split(",")
                    if (parts.size >= 3) NotificationSchedule(
                        id       = idx,
                        hour     = parts[0].toIntOrNull() ?: 8,
                        minute   = parts[1].toIntOrNull() ?: 0,
                        enabled  = parts[2] == "1",
                        category = safe(parts.getOrNull(3)) { s -> VerseCategory.valueOf(s) } ?: VerseCategory.ALL,
                        label    = parts.getOrNull(4) ?: ""
                    ) else null
                }
            }
        }

    suspend fun saveNotificationSchedules(schedules: List<NotificationSchedule>) {
        val encoded = schedules.joinToString("|") {
            "${it.hour},${it.minute},${if (it.enabled) "1" else "0"},${it.category.name},${it.label}"
        }
        context.dataStore.edit { it[Keys.NOTIFICATION_SCHEDULES] = encoded }
    }

    suspend fun saveNotificationSettings(
        enabled: Boolean, hour: Int, minute: Int = 0,
        frequency: NotificationFrequency = NotificationFrequency.ONCE_DAILY,
        category: VerseCategory = VerseCategory.ALL
    ) {
        context.dataStore.edit { p ->
            p[Keys.NOTIFICATIONS_ENABLED]  = enabled
            p[Keys.NOTIFICATION_HOUR]      = hour
            p[Keys.NOTIFICATION_MINUTE]    = minute
            p[Keys.NOTIFICATION_FREQUENCY] = frequency.name
            p[Keys.NOTIFICATION_CATEGORY]  = category.name
        }
    }

    // ── User Profile ──────────────────────────────────────────────
    val userProfile: Flow<UserProfile> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { p ->
            UserProfile(
                name            = p[Keys.USER_NAME] ?: "",
                ageGroup        = safe(p[Keys.AGE_GROUP]) { AgeGroup.valueOf(it) },
                faithJourney    = safe(p[Keys.FAITH_JOURNEY]) { FaithJourney.valueOf(it) },
                lifeSeason      = safe(p[Keys.LIFE_SEASON]) { LifeSeason.valueOf(it) },
                primaryGoal     = safe(p[Keys.PRIMARY_GOAL]) { SpiritualGoal.valueOf(it) },
                secondaryGoals  = (p[Keys.SECONDARY_GOALS] ?: "").split(",")
                    .filter { it.isNotBlank() }
                    .mapNotNull { s -> safe(s) { SpiritualGoal.valueOf(it) } },
                tradition       = safe(p[Keys.TRADITION]) { ChurchTradition.valueOf(it) },
                readingPace     = safe(p[Keys.READING_PACE]) { ReadingPace.valueOf(it) } ?: ReadingPace.LIGHT,
                favoriteTranslation = BibleTranslation.fromAbbreviation(p[Keys.DEFAULT_TRANSLATION] ?: "KJV"),
                streakDays      = p[Keys.STREAK_DAYS] ?: 0,
                totalVersesRead = p[Keys.TOTAL_VERSES_READ] ?: 0,
                onboardingComplete = p[Keys.ONBOARDING_DONE] ?: false
            )
        }

    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { p ->
            p[Keys.USER_NAME]          = profile.name
            profile.ageGroup?.let    { p[Keys.AGE_GROUP]      = it.name }
            profile.faithJourney?.let { p[Keys.FAITH_JOURNEY] = it.name }
            profile.lifeSeason?.let  { p[Keys.LIFE_SEASON]    = it.name }
            profile.primaryGoal?.let { p[Keys.PRIMARY_GOAL]   = it.name }
            if (profile.secondaryGoals.isNotEmpty())
                p[Keys.SECONDARY_GOALS] = profile.secondaryGoals.joinToString(",") { it.name }
            profile.tradition?.let   { p[Keys.TRADITION]      = it.name }
            p[Keys.READING_PACE]       = profile.readingPace.name
            p[Keys.DEFAULT_TRANSLATION] = profile.favoriteTranslation.abbreviation
            p[Keys.ONBOARDING_DONE]    = true
        }
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }.map { it[Keys.ONBOARDING_DONE] ?: false }

    // ── User Stats ────────────────────────────────────────────────
    val userName: Flow<String>  = context.dataStore.data.catch { emit(emptyPreferences()) }.map { it[Keys.USER_NAME] ?: "" }
    val streakDays: Flow<Int>   = context.dataStore.data.catch { emit(emptyPreferences()) }.map { it[Keys.STREAK_DAYS] ?: 0 }
    val totalVersesRead: Flow<Int> = context.dataStore.data.catch { emit(emptyPreferences()) }.map { it[Keys.TOTAL_VERSES_READ] ?: 0 }
    val randomizeOnOpen: Flow<Boolean> = context.dataStore.data.catch { emit(emptyPreferences()) }.map { it[Keys.RANDOMIZE_ON_OPEN] ?: false }
    val appThemeDark: Flow<Boolean>    = context.dataStore.data.catch { emit(emptyPreferences()) }.map { it[Keys.APP_THEME_DARK] ?: false }
    val hasPremium: Flow<Boolean>      = context.dataStore.data.catch { emit(emptyPreferences()) }.map { it[Keys.HAS_PREMIUM] ?: false }
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.catch { emit(emptyPreferences()) }.map { it[Keys.HAS_SEEN_ONBOARDING] ?: false }
    val defaultTranslation: Flow<BibleTranslation> = context.dataStore.data.catch { emit(emptyPreferences()) }
        .map { BibleTranslation.fromAbbreviation(it[Keys.DEFAULT_TRANSLATION] ?: "KJV") }

    suspend fun saveUserName(name: String)         = context.dataStore.edit { it[Keys.USER_NAME] = name }
    suspend fun setRandomizeOnOpen(v: Boolean)     = context.dataStore.edit { it[Keys.RANDOMIZE_ON_OPEN] = v }
    suspend fun setAppThemeDark(v: Boolean)        = context.dataStore.edit { it[Keys.APP_THEME_DARK] = v }
    suspend fun setPremium(value: Boolean)         = context.dataStore.edit { it[Keys.HAS_PREMIUM] = value }
    suspend fun markOnboardingSeen()               = context.dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = true }
    suspend fun setDefaultTranslation(t: BibleTranslation) = context.dataStore.edit { it[Keys.DEFAULT_TRANSLATION] = t.abbreviation }

    suspend fun incrementVersesRead() {
        context.dataStore.edit { p -> p[Keys.TOTAL_VERSES_READ] = (p[Keys.TOTAL_VERSES_READ] ?: 0) + 1 }
    }

    suspend fun updateStreak() {
        val now = System.currentTimeMillis()
        context.dataStore.edit { p ->
            val last = p[Keys.LAST_OPEN_DATE] ?: 0L
            val dayMs = 86_400_000L
            val daysSince = (now - last) / dayMs
            p[Keys.STREAK_DAYS] = when {
                daysSince == 1L -> (p[Keys.STREAK_DAYS] ?: 0) + 1
                daysSince == 0L -> p[Keys.STREAK_DAYS] ?: 0
                else -> 1
            }
            p[Keys.LAST_OPEN_DATE] = now
        }
    }
}

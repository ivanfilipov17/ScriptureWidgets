// domain/model/Models.kt
package com.scripturewidgets.domain.model

import androidx.compose.ui.graphics.Color

// ── Bible Verse ───────────────────────────────────────────────────
data class BibleVerse(
    val id: String,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val translation: BibleTranslation,
    val category: VerseCategory
) {
    val reference: String get() = "$book $chapter:$verse"
    val fullReference: String get() = "$book $chapter:$verse (${translation.abbreviation})"
    val shareText: String get() = "\"$text\"\n— $fullReference\n\nShared via Scripture Widgets"
}

enum class BibleTranslation(val abbreviation: String, val fullName: String) {
    KJV("KJV", "King James Version"),
    NIV("NIV", "New International Version"),
    ESV("ESV", "English Standard Version"),
    NLT("NLT", "New Living Translation"),
    NASB("NASB", "New American Standard Bible");
    companion object {
        fun fromAbbreviation(abbr: String) = entries.firstOrNull { it.abbreviation == abbr } ?: KJV
    }
}

enum class VerseCategory(val displayName: String, val icon: String, val colorHex: Long) {
    ALL("All",               "apps",                       0xFF607D8B),
    HOPE("Hope",             "wb_sunny",                   0xFFFF9800),
    FAITH("Faith",           "church",                     0xFF3F51B5),
    LOVE("Love",             "favorite",                   0xFFE91E63),
    STRENGTH("Strength",     "fitness_center",             0xFF4CAF50),
    PEACE("Peace",           "spa",                        0xFF00BCD4),
    WISDOM("Wisdom",         "auto_stories",               0xFF9C27B0),
    PRAYER("Prayer",         "self_improvement",           0xFF795548),
    SALVATION("Salvation",   "star",                       0xFFFFD700),
    GRATITUDE("Gratitude",   "volunteer_activism",         0xFF8BC34A),
    COURAGE("Courage",       "shield",                     0xFFF44336),
    HEALING("Healing",       "healing",                    0xFF009688),
    JOY("Joy",               "sentiment_very_satisfied",   0xFFFFEB3B),
    COMFORT("Comfort",       "home",                       0xFFFF7043),
    FORGIVENESS("Forgiveness","handshake",                 0xFF26C6DA);
    val composeColor: Color get() = Color(colorHex)
}

enum class WidgetTheme(val displayName: String, val isPremium: Boolean, val startColorHex: Long, val endColorHex: Long, val textColorHex: Long) {
    SUNRISE("Sunrise",    false, 0xFFFF6B35, 0xFFFFD166, 0xFFFFFFFF),
    FOREST("Forest",      false, 0xFF2D6A4F, 0xFF52B788, 0xFFFFFFFF),
    OCEAN("Ocean",        false, 0xFF0077B6, 0xFF90E0EF, 0xFFFFFFFF),
    MIDNIGHT("Midnight",  false, 0xFF1A1A2E, 0xFF16213E, 0xFFE0E0FF),
    PARCHMENT("Parchment",false, 0xFFF5E6C8, 0xFFE8D5A3, 0xFF3E2723),
    ROSE("Rose",          false, 0xFFFF758C, 0xFFFF7EB3, 0xFFFFFFFF),
    LAVENDER("Lavender",  true,  0xFF7B5EA7, 0xFFB388FF, 0xFFFFFFFF),
    GOLDEN("Golden",      true,  0xFFB8860B, 0xFFFFD700, 0xFF3E2723),
    CROSS("Cross",        true,  0xFF6D1B7B, 0xFF9C27B0, 0xFFFFFFFF),
    DARK_GOLD("Dark Gold",true,  0xFF1C1C1C, 0xFF3D2B00, 0xFFFFD700),
    MINT("Mint",          false, 0xFF26A69A, 0xFF80CBC4, 0xFFFFFFFF),
    CUSTOM("Custom",      false, 0xFF455A64, 0xFF90A4AE, 0xFFFFFFFF);
}

enum class WidgetFontStyle(val displayName: String) {
    SERIF("Serif"), SANS_SERIF("Sans Serif"), ITALIC("Italic"),
    ELEGANT("Elegant"), BOLD("Bold"), LIGHT("Light")
}

enum class WidgetContentType(val displayName: String) {
    DAILY_VERSE("Daily Verse"), RANDOM_VERSE("Random Verse"),
    MORNING_PRAYER("Morning Prayer"), EVENING_PRAYER("Evening Prayer"),
    CHRISTIAN_QUOTE("Christian Quote"), VERSE_BY_CATEGORY("Verse by Category"),
    VERSE_OF_WEEK("Verse of the Week"), MEMORIZATION("Memorization Mode")
}

enum class NotificationFrequency(val displayName: String, val timesPerDay: Int) {
    ONCE("Once a day", 1), TWICE("Twice a day", 2),
    THREE_TIMES("3 times a day", 3), HOURLY("Every few hours", 6), CUSTOM("Custom schedule", 0)
}

enum class WidgetTextAlign(val displayName: String) { CENTER("Center"), LEFT("Left"), RIGHT("Right") }

enum class WidgetBackground(val displayName: String, val isPremium: Boolean) {
    GRADIENT("Gradient", false), SOLID("Solid Color", false), BLUR("Frosted Glass", true),
    DARK_BLUR("Dark Blur", true), NATURE("Nature Pattern", true),
    MINIMAL("Minimal White", false), MINIMAL_DARK("Minimal Dark", false)
}

data class NotificationSchedule(
    val id: Int, val hour: Int, val minute: Int, val enabled: Boolean,
    val category: VerseCategory = VerseCategory.ALL, val label: String = ""
)

data class WidgetConfig(
    val theme: WidgetTheme            = WidgetTheme.SUNRISE,
    val fontStyle: WidgetFontStyle    = WidgetFontStyle.SERIF,
    val fontSize: Float               = 14f,
    val translation: BibleTranslation = BibleTranslation.KJV,
    val contentType: WidgetContentType = WidgetContentType.DAILY_VERSE,
    val category: VerseCategory       = VerseCategory.ALL,
    val showReference: Boolean        = true,
    val showTranslation: Boolean      = true,
    val textAlign: WidgetTextAlign    = WidgetTextAlign.CENTER,
    val background: WidgetBackground  = WidgetBackground.GRADIENT,
    val customBgColorHex: Long        = 0xFF1A237E,
    val customTextColorHex: Long      = 0xFFFFFFFF,
    val showVerseNumber: Boolean      = true,
    val compactMode: Boolean          = false
)

data class DailyPrayer(val id: String, val title: String, val text: String, val type: PrayerType)

enum class PrayerType(val displayName: String, val icon: String) {
    MORNING("Morning", "wb_sunny"), EVENING("Evening", "nights_stay"),
    GRATITUDE("Gratitude", "favorite"), HEALING("Healing", "healing"),
    PROTECTION("Protection", "shield")
}

data class ChristianQuote(val id: String, val text: String, val author: String)
data class FavoriteVerse(val id: String, val verse: BibleVerse, val savedDate: Long, val notes: String = "")

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// ── Personalisation Enums ─────────────────────────────────────────
enum class AgeGroup(val displayName: String, val range: String) {
    TEEN("Teen", "13-17"), YOUNG_ADULT("Young Adult", "18-25"),
    ADULT("Adult", "26-40"), MIDDLE_AGED("Middle Aged", "41-60"), SENIOR("Senior", "60+")
}

enum class FaithJourney(val displayName: String, val description: String) {
    EXPLORING("Exploring Faith",   "I'm curious about Christianity"),
    NEW_BELIEVER("New Believer",   "I recently accepted Christ"),
    GROWING("Growing",             "I've been a Christian for a few years"),
    ESTABLISHED("Established",     "I have a deep, mature faith"),
    RETURNING("Returning",         "I'm reconnecting after time away")
}

enum class LifeSeason(val displayName: String, val description: String) {
    STUDENT("Student",           "School, college or university"),
    CAREER("Building Career",    "Working toward professional goals"),
    FAMILY("Family Life",        "Raising children or caring for family"),
    RELATIONSHIP("Relationship", "Marriage, dating or relationships"),
    GRIEF("Grief & Loss",        "Processing loss or hardship"),
    HEALTH("Health Challenge",   "Dealing with illness or recovery"),
    TRANSITION("Life Transition","Major change - job, move, new chapter"),
    RETIREMENT("Retirement",     "Enjoying a new season of life"),
    MINISTRY("Ministry",         "Serving in church or outreach"),
    GENERAL("General",           "Day-to-day life")
}

enum class SpiritualGoal(
    val displayName: String,
    val description: String,
    val recommendedCategories: List<VerseCategory>
) {
    DAILY_DEVOTION("Daily Devotion",      "Start each day grounded in Scripture",
        listOf(VerseCategory.FAITH, VerseCategory.HOPE, VerseCategory.GRATITUDE)),
    MEMORIZATION("Memorize Scripture",    "Learn and hold God's Word in my heart",
        listOf(VerseCategory.WISDOM, VerseCategory.FAITH, VerseCategory.SALVATION)),
    FIND_PEACE("Find Peace",              "Overcome anxiety and find calm in God",
        listOf(VerseCategory.PEACE, VerseCategory.COMFORT, VerseCategory.HOPE)),
    GROW_FAITH("Grow in Faith",           "Deepen my relationship with God",
        listOf(VerseCategory.FAITH, VerseCategory.WISDOM, VerseCategory.PRAYER)),
    FIND_STRENGTH("Find Strength",        "Get through a difficult season",
        listOf(VerseCategory.STRENGTH, VerseCategory.COURAGE, VerseCategory.HEALING)),
    SHARE_FAITH("Share My Faith",         "Be equipped to share the Gospel",
        listOf(VerseCategory.SALVATION, VerseCategory.LOVE, VerseCategory.FAITH)),
    PRAYER_LIFE("Deepen Prayer",          "Strengthen my prayer life",
        listOf(VerseCategory.PRAYER, VerseCategory.FAITH, VerseCategory.GRATITUDE)),
    HEALING("Healing & Recovery",         "Find God's comfort in pain or illness",
        listOf(VerseCategory.HEALING, VerseCategory.COMFORT, VerseCategory.HOPE)),
    JOY("Experience Joy",                 "Cultivate gratitude and joy daily",
        listOf(VerseCategory.JOY, VerseCategory.GRATITUDE, VerseCategory.LOVE)),
    FORGIVENESS("Walk in Forgiveness",    "Release bitterness and embrace grace",
        listOf(VerseCategory.FORGIVENESS, VerseCategory.LOVE, VerseCategory.PEACE))
}

enum class ReadingPace(val displayName: String, val description: String, val timesPerDay: Int) {
    LIGHT("Light",       "1 verse - a gentle daily reminder",  1),
    MODERATE("Moderate", "Morning and evening verses",         2),
    IMMERSIVE("Deep",    "4+ verses throughout the day",       4)
}

enum class ChurchTradition(val displayName: String) {
    NON_DENOMINATIONAL("Non-Denominational"), CATHOLIC("Catholic"), BAPTIST("Baptist"),
    PENTECOSTAL("Pentecostal / Charismatic"), METHODIST("Methodist"), LUTHERAN("Lutheran"),
    PRESBYTERIAN("Presbyterian"), ANGLICAN("Anglican / Episcopal"),
    ORTHODOX("Orthodox"), OTHER("Other / Prefer not to say")
}

// ── User Profile ──────────────────────────────────────────────────
data class UserProfile(
    val name: String                            = "",
    val ageGroup: AgeGroup?                     = null,
    val faithJourney: FaithJourney?             = null,
    val lifeSeason: LifeSeason?                 = null,
    val primaryGoal: SpiritualGoal?             = null,
    val secondaryGoals: List<SpiritualGoal>     = emptyList(),
    val tradition: ChurchTradition?             = null,
    val readingPace: ReadingPace                = ReadingPace.LIGHT,
    val favoriteTranslation: BibleTranslation   = BibleTranslation.KJV,
    val favoriteCategories: List<VerseCategory> = emptyList(),
    val streakDays: Int                         = 0,
    val totalVersesRead: Int                    = 0,
    val joinDate: Long                          = System.currentTimeMillis(),
    val onboardingComplete: Boolean             = false
) {
    val recommendedCategories: List<VerseCategory> get() {
        val cats = mutableSetOf<VerseCategory>()
        primaryGoal?.recommendedCategories?.let { cats.addAll(it) }
        secondaryGoals.forEach { goal -> cats.addAll(goal.recommendedCategories) }
        cats.addAll(favoriteCategories)
        when (ageGroup) {
            AgeGroup.TEEN   -> cats.addAll(listOf(VerseCategory.WISDOM, VerseCategory.COURAGE))
            AgeGroup.SENIOR -> cats.addAll(listOf(VerseCategory.PEACE, VerseCategory.GRATITUDE))
            else -> {}
        }
        when (lifeSeason) {
            LifeSeason.GRIEF      -> cats.addAll(listOf(VerseCategory.COMFORT, VerseCategory.HOPE))
            LifeSeason.HEALTH     -> cats.addAll(listOf(VerseCategory.HEALING, VerseCategory.STRENGTH))
            LifeSeason.FAMILY     -> cats.addAll(listOf(VerseCategory.LOVE, VerseCategory.WISDOM))
            LifeSeason.MINISTRY   -> cats.addAll(listOf(VerseCategory.FAITH, VerseCategory.SALVATION))
            else -> {}
        }
        return cats.toList().ifEmpty { listOf(VerseCategory.ALL) }
    }

    val motivationalTagline: String get() = when (primaryGoal) {
        SpiritualGoal.FIND_PEACE     -> "\"Peace I leave with you\" - John 14:27"
        SpiritualGoal.GROW_FAITH     -> "\"Faith comes by hearing\" - Romans 10:17"
        SpiritualGoal.FIND_STRENGTH  -> "\"I can do all things through Christ\" - Phil 4:13"
        SpiritualGoal.HEALING        -> "\"He heals the brokenhearted\" - Psalm 147:3"
        SpiritualGoal.JOY            -> "\"The joy of the Lord is your strength\" - Neh 8:10"
        SpiritualGoal.DAILY_DEVOTION -> "\"Your word is a lamp unto my feet\" - Psalm 119:105"
        SpiritualGoal.PRAYER_LIFE    -> "\"Pray without ceasing\" - 1 Thessalonians 5:17"
        SpiritualGoal.FORGIVENESS    -> "\"Forgive as the Lord has forgiven you\" - Col 3:13"
        SpiritualGoal.MEMORIZATION   -> "\"I have hidden your word in my heart\" - Psalm 119:11"
        SpiritualGoal.SHARE_FAITH    -> "\"Go and make disciples of all nations\" - Matt 28:19"
        null -> "\"The word of God is living and active\" - Hebrews 4:12"
    }

    val personalGreeting: String get() = when {
        name.isNotBlank() -> "Welcome back, $name"
        faithJourney == FaithJourney.EXPLORING -> "Welcome, Seeker"
        faithJourney == FaithJourney.NEW_BELIEVER -> "Welcome, New Believer"
        else -> "Welcome back"
    }
}

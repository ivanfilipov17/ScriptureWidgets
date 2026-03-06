package com.scripturewidgets.presentation.screens.today

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scripturewidgets.domain.model.*
import com.scripturewidgets.presentation.viewmodel.TodayUiState
import com.scripturewidgets.presentation.viewmodel.TodayViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayScreen(viewModel: TodayViewModel = hiltViewModel()) {
    val uiState   by viewModel.uiState.collectAsState()
    val config    by viewModel.widgetConfig.collectAsState()
    val profile   by viewModel.userProfile.collectAsState()
    val streak    by viewModel.streakDays.collectAsState()
    val versesRead by viewModel.totalVersesRead.collectAsState()

    val state         = uiState
    val currentTheme  = config.theme
    val currentProfile = profile

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // ── Personalized Header ────────────────────────────────────
        PersonalizedHeader(profile = currentProfile, streak = streak, versesRead = versesRead, theme = currentTheme)

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // ── Goal Progress Banner ───────────────────────────────
            val primaryGoal = currentProfile.primaryGoal
            if (primaryGoal != null) {
                GoalBanner(goal = primaryGoal, streak = streak)
            }

            // ── Verse Card ─────────────────────────────────────────
            when {
                state.isLoading -> Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.dailyVerse != null -> {
                    val verse = state.dailyVerse
                    val isFavorite by viewModel.isFavorite(verse.id).collectAsState(initial = false)
                    HeroVerseCard(
                        verse       = verse,
                        theme       = currentTheme,
                        isFavorite  = isFavorite,
                        profile     = currentProfile,
                        onFavorite  = { viewModel.toggleFavorite(verse) },
                        onShare     = { viewModel.shareVerse(verse) },
                        onRefresh   = { viewModel.loadRandomVerse() }
                    )
                }
                state.error != null -> ErrorCard(state.error) { viewModel.loadDailyVerse() }
            }

            // ── Category quick-picks based on profile ──────────────
            val recCats = currentProfile.recommendedCategories
            if (recCats.size > 1) {
                RecommendedTopics(
                    categories = recCats.take(5),
                    onCategoryTap = { /* navigate to browse filtered */ }
                )
            }

            // ── Life-season specific encouragement ─────────────────
            val lifeSeason = currentProfile.lifeSeason
            if (lifeSeason != null && lifeSeason != LifeSeason.GENERAL) {
                SeasonEncouragementCard(lifeSeason)
            }

            // ── Widget hint ────────────────────────────────────────
            WidgetHintCard(themeName = currentTheme.displayName)
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Personalized Header ───────────────────────────────────────────
@Composable
private fun PersonalizedHeader(profile: UserProfile, streak: Int, versesRead: Int, theme: WidgetTheme) {
    val now       = remember { Date() }
    val hour      = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting  = when {
        profile.name.isNotBlank() && hour < 12  -> "Good morning, ${profile.name}"
        profile.name.isNotBlank() && hour < 17  -> "Good afternoon, ${profile.name}"
        profile.name.isNotBlank()               -> "Good evening, ${profile.name}"
        hour < 12                               -> "Good morning"
        hour < 17                               -> "Good afternoon"
        else                                    -> "Good evening"
    }
    Box(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(theme.startColorHex), Color(theme.startColorHex).copy(.6f))))
            .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now),
                style = MaterialTheme.typography.labelMedium, color = Color(theme.textColorHex).copy(.75f))
            Text(greeting, style = MaterialTheme.typography.headlineSmall,
                color = Color(theme.textColorHex), fontFamily = FontFamily.Serif)
            if (profile.primaryGoal != null) {
                Text("Focus: ${profile.primaryGoal.displayName}",
                    style = MaterialTheme.typography.labelMedium, color = Color(theme.textColorHex).copy(.8f))
            }
            if (streak > 0 || versesRead > 0) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (streak > 0) MiniStat("🔥 $streak day streak")
                    if (versesRead > 0) MiniStat("📖 $versesRead verses read")
                }
            }
        }
    }
}

@Composable
private fun MiniStat(text: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(.2f)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

// ── Goal Banner ───────────────────────────────────────────────────
@Composable
private fun GoalBanner(goal: SpiritualGoal, streak: Int) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(.5f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("🎯", fontSize = 22.sp)
            Column(Modifier.weight(1f)) {
                Text(goal.displayName, style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(goal.description, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.7f))
            }
            if (streak > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("days", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.7f))
                }
            }
        }
    }
}

// ── Hero Verse Card ───────────────────────────────────────────────
@Composable
private fun HeroVerseCard(
    verse: BibleVerse, theme: WidgetTheme, isFavorite: Boolean,
    profile: UserProfile, onFavorite: () -> Unit, onShare: () -> Unit, onRefresh: () -> Unit
) {
    val startColor = Color(theme.startColorHex)
    val endColor   = Color(theme.endColorHex)
    val textColor  = Color(theme.textColorHex)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(verse.id) { visible = true }

    AnimatedVisibility(visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(8.dp)) {
            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(startColor, endColor))).padding(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    // Category badge
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(.2f)) {
                        Text(verse.category.displayName, Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, color = textColor)
                    }

                    Text("\u201C${verse.text}\u201D",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = if (profile.ageGroup == AgeGroup.SENIOR) 18.sp else 16.sp),
                        fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                        color = textColor, textAlign = TextAlign.Center, lineHeight = 28.sp)

                    Text(verse.fullReference, style = MaterialTheme.typography.labelLarge,
                        color = textColor.copy(.85f), fontFamily = FontFamily.Serif)

                    // Reflection prompt based on goal
                    val theGoal = profile.primaryGoal
                    if (theGoal != null) {
                        val prompt = getReflectionPrompt(theGoal, verse.category)
                        if (prompt.isNotBlank()) {
                            HorizontalDivider(color = textColor.copy(.2f))
                            Text(prompt, style = MaterialTheme.typography.labelMedium,
                                color = textColor.copy(.8f), textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Italic)
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        VerseActionButton(if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            if (isFavorite) "Saved" else "Save",
                            if (isFavorite) Color.Red else textColor, onFavorite)
                        VerseActionButton(Icons.Default.Share, "Share", textColor, onShare)
                        VerseActionButton(Icons.Default.Refresh, "New", textColor, onRefresh)
                    }
                }
            }
        }
    }
}

private fun getReflectionPrompt(goal: SpiritualGoal, category: VerseCategory): String = when (goal) {
    SpiritualGoal.MEMORIZATION   -> "💡 Try memorizing the first sentence today"
    SpiritualGoal.FIND_PEACE     -> "🕊️ Take a breath and let this truth settle in"
    SpiritualGoal.GROW_FAITH     -> "📝 How does this apply to your life today?"
    SpiritualGoal.PRAYER_LIFE    -> "🙏 Use this verse as your prayer opening today"
    SpiritualGoal.DAILY_DEVOTION -> "☀️ Carry this with you throughout your day"
    SpiritualGoal.FIND_STRENGTH  -> "💪 Speak this aloud as your declaration today"
    SpiritualGoal.HEALING        -> "❤️ Receive this as God's word directly to you"
    SpiritualGoal.JOY            -> "😊 Find one thing to be grateful for right now"
    else -> ""
}

// ── Recommended Topics ────────────────────────────────────────────
@Composable
private fun RecommendedTopics(categories: List<VerseCategory>, onCategoryTap: (VerseCategory) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Your Topics", style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(.7f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { cat ->
                Surface(shape = RoundedCornerShape(20.dp), color = Color(cat.colorHex).copy(.15f),
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))) {
                    Text(cat.displayName, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium, color = Color(cat.colorHex))
                }
            }
        }
    }
}

// ── Life Season Card ──────────────────────────────────────────────
@Composable
private fun SeasonEncouragementCard(season: LifeSeason) {
    val (message, emoji) = remember(season) { getSeasonMessage(season) }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(emoji, fontSize = 26.sp)
            Column(Modifier.weight(1f)) {
                Text(season.displayName, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Text(message, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(.7f))
            }
        }
    }
}

private fun getSeasonMessage(season: LifeSeason): Pair<String, String> = when (season) {
    LifeSeason.GRIEF       -> Pair("God is close to the brokenhearted. You are not alone.", "🤍")
    LifeSeason.HEALTH      -> Pair("Healing is God's desire for you. His strength is your strength.", "💚")
    LifeSeason.STUDENT     -> Pair("Wisdom begins with God. You are equipped for this season.", "📚")
    LifeSeason.FAMILY      -> Pair("A family that prays together grows together in love.", "👨‍👩‍👧")
    LifeSeason.TRANSITION  -> Pair("God's plans for you are good. Trust the process.", "🦋")
    LifeSeason.CAREER      -> Pair("Whatever you do, work at it wholeheartedly for God.", "💼")
    LifeSeason.MINISTRY    -> Pair("You are called and equipped to make a difference.", "⛪")
    LifeSeason.RETIREMENT  -> Pair("Your best days of bearing fruit are still ahead.", "🌸")
    else -> Pair("God walks with you in every step of life.", "✨")
}

// ── Shared components ─────────────────────────────────────────────
@Composable
private fun VerseActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FilledTonalIconButton(onClick = onClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.White.copy(.2f), contentColor = tint)) { Icon(icon, label) }
        Text(label, color = tint, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun WidgetHintCard(themeName: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Widget Preview", style = MaterialTheme.typography.titleMedium)
            Text("Long-press your home screen → Widgets → Scripture Widgets to add.",
                style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Widgets, null, tint = MaterialTheme.colorScheme.primary)
                Text("Theme: $themeName", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

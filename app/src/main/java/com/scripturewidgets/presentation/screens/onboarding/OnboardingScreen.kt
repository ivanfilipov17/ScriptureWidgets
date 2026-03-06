package com.scripturewidgets.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scripturewidgets.data.PreferencesRepository
import com.scripturewidgets.domain.model.*
import com.scripturewidgets.presentation.screens.settings.SettingsCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════════════════════════════
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: PreferencesRepository
) : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    fun setName(name: String)                        = _profile.update { it.copy(name = name) }
    fun setAgeGroup(ag: AgeGroup)                    = _profile.update { it.copy(ageGroup = ag) }
    fun setFaithJourney(fj: FaithJourney)            = _profile.update { it.copy(faithJourney = fj) }
    fun setLifeSeason(ls: LifeSeason)                = _profile.update { it.copy(lifeSeason = ls) }
    fun setPrimaryGoal(g: SpiritualGoal)             = _profile.update { it.copy(primaryGoal = g) }
    fun setTradition(t: ChurchTradition)             = _profile.update { it.copy(tradition = t) }
    fun setReadingPace(p: ReadingPace)               = _profile.update { it.copy(readingPace = p) }
    fun setTranslation(t: BibleTranslation)          = _profile.update { it.copy(favoriteTranslation = t) }

    fun toggleSecondaryGoal(g: SpiritualGoal) {
        _profile.update { profile ->
            val existing = profile.secondaryGoals.toMutableList()
            if (existing.contains(g)) existing.remove(g) else if (existing.size < 3) existing.add(g)
            profile.copy(secondaryGoals = existing)
        }
    }

    fun saveAndFinish() = viewModelScope.launch {
        preferences.saveUserProfile(_profile.value)
        // Auto-apply recommended settings based on profile
        val p = _profile.value
        val recommendedConfig = WidgetConfig(
            category    = p.recommendedCategories.firstOrNull() ?: VerseCategory.ALL,
            contentType = when (p.primaryGoal) {
                SpiritualGoal.MEMORIZATION -> WidgetContentType.MEMORIZATION
                SpiritualGoal.DAILY_DEVOTION -> WidgetContentType.DAILY_VERSE
                else -> WidgetContentType.VERSE_BY_CATEGORY
            },
            fontSize    = when (p.ageGroup) {
                AgeGroup.SENIOR -> 16f
                AgeGroup.TEEN   -> 13f
                else            -> 14f
            },
            translation = p.favoriteTranslation,
            theme       = when (p.primaryGoal) {
                SpiritualGoal.FIND_PEACE    -> WidgetTheme.OCEAN
                SpiritualGoal.FIND_STRENGTH -> WidgetTheme.FOREST
                SpiritualGoal.JOY           -> WidgetTheme.SUNRISE
                SpiritualGoal.HEALING       -> WidgetTheme.MINT
                else                        -> WidgetTheme.SUNRISE
            }
        )
        preferences.saveWidgetConfig(recommendedConfig)
        // Schedule notifications based on reading pace
        val schedules = when (p.readingPace) {
            ReadingPace.LIGHT    -> listOf(
                NotificationSchedule(0, 8, 0, true, p.recommendedCategories.firstOrNull() ?: VerseCategory.ALL, "Morning")
            )
            ReadingPace.MODERATE -> listOf(
                NotificationSchedule(0, 8,  0, true, p.recommendedCategories.firstOrNull() ?: VerseCategory.ALL, "Morning"),
                NotificationSchedule(1, 20, 0, true, p.recommendedCategories.getOrElse(1) { VerseCategory.ALL }, "Evening")
            )
            ReadingPace.IMMERSIVE -> listOf(
                NotificationSchedule(0, 7,  0, true, VerseCategory.ALL, "Morning"),
                NotificationSchedule(1, 12, 0, true, VerseCategory.ALL, "Midday"),
                NotificationSchedule(2, 18, 0, true, VerseCategory.ALL, "Afternoon"),
                NotificationSchedule(3, 21, 0, true, VerseCategory.ALL, "Evening")
            )
        }
        preferences.saveNotificationSchedules(schedules)
        preferences.saveNotificationSettings(true, schedules.first().hour, schedules.first().minute)
    }
}

// ═══════════════════════════════════════════════════════════════
// Main Onboarding Screen
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 8 })
    val scope = rememberCoroutineScope()

    fun next() = scope.launch {
        if (pagerState.currentPage < 7) pagerState.animateScrollToPage(pagerState.currentPage + 1)
        else { viewModel.saveAndFinish(); onComplete() }
    }
    fun skip() = scope.launch { viewModel.saveAndFinish(); onComplete() }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = false) { page ->
            when (page) {
                0 -> WelcomePage(onNext = { next() })
                1 -> NamePage(name = profile.name, onNameChange = viewModel::setName, onNext = { next() })
                2 -> AgeGroupPage(selected = profile.ageGroup, onSelect = viewModel::setAgeGroup, onNext = { next() }, onSkip = { skip() })
                3 -> FaithJourneyPage(selected = profile.faithJourney, onSelect = viewModel::setFaithJourney, onNext = { next() }, onSkip = { skip() })
                4 -> LifeSeasonPage(selected = profile.lifeSeason, onSelect = viewModel::setLifeSeason, onNext = { next() }, onSkip = { skip() })
                5 -> GoalsPage(primary = profile.primaryGoal, secondary = profile.secondaryGoals, onPrimary = viewModel::setPrimaryGoal, onToggleSecondary = viewModel::toggleSecondaryGoal, onNext = { next() }, onSkip = { skip() })
                6 -> PreferencesPage(pace = profile.readingPace, translation = profile.favoriteTranslation, tradition = profile.tradition, onPace = viewModel::setReadingPace, onTranslation = viewModel::setTranslation, onTradition = viewModel::setTradition, onNext = { next() }, onSkip = { skip() })
                7 -> SummaryPage(profile = profile, onFinish = { next() })
            }
        }

        // Progress dots
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(8) { i ->
                val isActive = i == pagerState.currentPage
                val width by animateDpAsState(if (isActive) 24.dp else 8.dp, label = "dot")
                Box(Modifier.height(8.dp).width(width).clip(CircleShape)
                    .background(if (isActive) Color.White else Color.White.copy(.35f)))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 1 — Welcome
// ═══════════════════════════════════════════════════════════════
@Composable
private fun WelcomePage(onNext: () -> Unit) {
    GradientPage(listOf(Color(0xFF614385), Color(0xFF516395))) {
        Spacer(Modifier.weight(1f))
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }
        AnimatedVisibility(visible, enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 2 }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 32.dp)) {
                Text("✝", fontSize = 64.sp, color = Color.White)
                Text("Scripture Widgets", style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Serif, color = Color.White, textAlign = TextAlign.Center)
                Text("God's Word. Your way.\nPersonalized for your faith journey.",
                    style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(.85f),
                    textAlign = TextAlign.Center, lineHeight = 26.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF614385))) {
            Text("Get Started", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, null)
        }
        Spacer(Modifier.height(80.dp))
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 2 — Name
// ═══════════════════════════════════════════════════════════════
@Composable
private fun NamePage(name: String, onNameChange: (String) -> Unit, onNext: () -> Unit) {
    GradientPage(listOf(Color(0xFF134E5E), Color(0xFF71B280))) {
        PageHeader("What should we call you?", "This helps us personalize your experience", "👤")
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = name, onValueChange = onNameChange,
            label = { Text("Your first name") },
            placeholder = { Text("e.g. David") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White.copy(.8f),
                focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(.5f),
                cursorColor = Color.White, focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(.7f), focusedPlaceholderColor = Color.White.copy(.5f),
                unfocusedPlaceholderColor = Color.White.copy(.4f)
            )
        )
        Spacer(Modifier.weight(1f))
        PageButtons(onNext = onNext, onSkip = onNext, nextLabel = if (name.isBlank()) "Skip" else "Continue")
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 3 — Age Group
// ═══════════════════════════════════════════════════════════════
@Composable
private fun AgeGroupPage(selected: AgeGroup?, onSelect: (AgeGroup) -> Unit, onNext: () -> Unit, onSkip: () -> Unit) {
    GradientPage(listOf(Color(0xFFf7971e), Color(0xFFffd200))) {
        PageHeader("How old are you?", "We'll tailor verse complexity and themes to your life stage", "📅")
        Spacer(Modifier.height(24.dp))
        Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AgeGroup.entries.forEach { ag ->
                SelectionCard(
                    title    = ag.displayName,
                    subtitle = ag.range,
                    isSelected = selected == ag,
                    onClick  = { onSelect(ag) },
                    accentColor = Color(0xFFf7971e)
                )
            }
        }
        Spacer(Modifier.weight(1f))
        PageButtons(onNext = onNext, onSkip = onSkip, nextEnabled = selected != null)
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 4 — Faith Journey
// ═══════════════════════════════════════════════════════════════
@Composable
private fun FaithJourneyPage(selected: FaithJourney?, onSelect: (FaithJourney) -> Unit, onNext: () -> Unit, onSkip: () -> Unit) {
    GradientPage(listOf(Color(0xFF360033), Color(0xFF0b8793))) {
        PageHeader("Where are you in your faith?", "No judgment — this helps us meet you exactly where you are", "🙏")
        Spacer(Modifier.height(24.dp))
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FaithJourney.entries.forEach { fj ->
                SelectionCard(
                    title    = fj.displayName,
                    subtitle = fj.description,
                    isSelected = selected == fj,
                    onClick  = { onSelect(fj) },
                    accentColor = Color(0xFF0b8793)
                )
            }
            Spacer(Modifier.height(100.dp))
        }
        PageButtons(onNext = onNext, onSkip = onSkip, nextEnabled = selected != null)
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 5 — Life Season
// ═══════════════════════════════════════════════════════════════
@Composable
private fun LifeSeasonPage(selected: LifeSeason?, onSelect: (LifeSeason) -> Unit, onNext: () -> Unit, onSkip: () -> Unit) {
    GradientPage(listOf(Color(0xFF1A1A2E), Color(0xFF16213E))) {
        PageHeader("What season of life are you in?", "We'll suggest verses most relevant to where you are right now", "🌱")
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(LifeSeason.entries) { ls ->
                SeasonChip(season = ls, isSelected = selected == ls, onClick = { onSelect(ls) })
            }
        }
        PageButtons(onNext = onNext, onSkip = onSkip, nextEnabled = selected != null)
    }
}

@Composable
private fun SeasonChip(season: LifeSeason, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF3F51B5) else Color.White.copy(.1f)
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF90CAF9)) else null
    ) {
        Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.Center) {
            Text(season.displayName, style = MaterialTheme.typography.labelLarge,
                color = Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            Text(season.description, style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(.65f), lineHeight = 14.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 6 — Goals
// ═══════════════════════════════════════════════════════════════
@Composable
private fun GoalsPage(
    primary: SpiritualGoal?, secondary: List<SpiritualGoal>,
    onPrimary: (SpiritualGoal) -> Unit, onToggleSecondary: (SpiritualGoal) -> Unit,
    onNext: () -> Unit, onSkip: () -> Unit
) {
    GradientPage(listOf(Color(0xFF2D6A4F), Color(0xFF52B788))) {
        PageHeader("What's your main goal?", "We'll build your experience around this — pick one primary, up to 3 more", "🎯")
        Spacer(Modifier.height(16.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Primary Goal", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(.7f),
                modifier = Modifier.padding(bottom = 4.dp))
            SpiritualGoal.entries.forEach { goal ->
                val isPrimary    = primary == goal
                val isSecondary  = secondary.contains(goal)
                GoalCard(goal = goal, isPrimary = isPrimary, isSecondary = isSecondary,
                    onClickPrimary = { onPrimary(goal) },
                    onToggleSecondary = { if (!isPrimary) onToggleSecondary(goal) })
            }
            Spacer(Modifier.height(100.dp))
        }
        PageButtons(onNext = onNext, onSkip = onSkip, nextEnabled = primary != null)
    }
}

@Composable
private fun GoalCard(
    goal: SpiritualGoal, isPrimary: Boolean, isSecondary: Boolean,
    onClickPrimary: () -> Unit, onToggleSecondary: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = when {
            isPrimary   -> Color(0xFF1B5E20).copy(.9f)
            isSecondary -> Color(0xFF2E7D32).copy(.6f)
            else        -> Color.White.copy(.1f)
        }),
        border = when {
            isPrimary   -> BorderStroke(2.dp, Color(0xFF69F0AE))
            isSecondary -> BorderStroke(1.dp, Color(0xFF81C784))
            else        -> null
        }
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f).clickable(onClick = onClickPrimary)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isPrimary) Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Text(goal.displayName, style = MaterialTheme.typography.bodyMedium,
                        color = Color.White, fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal)
                }
                Text(goal.description, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(.65f))
            }
            if (!isPrimary) {
                Checkbox(checked = isSecondary, onCheckedChange = { onToggleSecondary() },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF69F0AE), checkmarkColor = Color.Black,
                        uncheckedColor = Color.White.copy(.5f)))
            } else {
                Surface(shape = CircleShape, color = Color(0xFFFFD700)) {
                    Text("Main", Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 7 — Preferences
// ═══════════════════════════════════════════════════════════════
@Composable
private fun PreferencesPage(
    pace: ReadingPace, translation: BibleTranslation, tradition: ChurchTradition?,
    onPace: (ReadingPace) -> Unit, onTranslation: (BibleTranslation) -> Unit,
    onTradition: (ChurchTradition) -> Unit, onNext: () -> Unit, onSkip: () -> Unit
) {
    GradientPage(listOf(Color(0xFF0077B6), Color(0xFF90E0EF))) {
        PageHeader("Your Reading Style", "Set how you want to engage with Scripture each day", "📖")
        Spacer(Modifier.height(16.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Reading Pace
            Text("How much Scripture do you want daily?", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(.8f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadingPace.entries.forEach { p ->
                    PaceChip(pace = p, isSelected = pace == p, onSelect = { onPace(p) },
                        modifier = Modifier.weight(1f))
                }
            }

            // Translation
            Text("Preferred Bible Translation", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(.8f))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BibleTranslation.entries) { t ->
                    FilterChip(selected = translation == t, onClick = { onTranslation(t) },
                        label = { Text(t.abbreviation, fontWeight = if (translation == t) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White, selectedLabelColor = Color(0xFF0077B6),
                            containerColor = Color.White.copy(.15f), labelColor = Color.White))
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BibleTranslation.entries) { t ->
                    Text(t.fullName, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(.5f))
                }
            }

            // Tradition (optional)
            Text("Church Background (optional)", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(.8f))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ChurchTradition.entries) { t ->
                    FilterChip(selected = tradition == t, onClick = { onTradition(t) },
                        label = { Text(t.displayName, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White, selectedLabelColor = Color(0xFF0077B6),
                            containerColor = Color.White.copy(.15f), labelColor = Color.White))
                }
            }
            Spacer(Modifier.height(100.dp))
        }
        PageButtons(onNext = onNext, onSkip = onSkip)
    }
}

@Composable
private fun PaceChip(pace: ReadingPace, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onSelect, modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color.White else Color.White.copy(.12f)),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else null) {
        Column(Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(pace.displayName, style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) Color(0xFF0077B6) else Color.White, fontWeight = FontWeight.Bold)
            Text(pace.description, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center,
                color = if (isSelected) Color(0xFF0077B6).copy(.7f) else Color.White.copy(.7f), lineHeight = 14.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 8 — Summary
// ═══════════════════════════════════════════════════════════════
@Composable
private fun SummaryPage(profile: UserProfile, onFinish: () -> Unit) {
    GradientPage(listOf(Color(0xFF614385), Color(0xFF516395))) {
        Spacer(Modifier.weight(0.5f))
        Column(Modifier.padding(horizontal = 28.dp), verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

            Text("✝", fontSize = 52.sp)
            Text(if (profile.name.isNotBlank()) "Welcome, ${profile.name}!" else "Welcome!",
                style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif,
                color = Color.White, textAlign = TextAlign.Center)
            Text(profile.motivationalTagline, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif,
                color = Color.White.copy(.85f), textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

            // Profile summary card
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(.15f)),
                shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Your personalized setup:", style = MaterialTheme.typography.labelLarge, color = Color.White)

                    profile.primaryGoal?.let { SummaryRow("🎯 Goal", it.displayName) }
                    profile.faithJourney?.let { SummaryRow("🙏 Faith", it.displayName) }
                    profile.lifeSeason?.let { SummaryRow("🌱 Season", it.displayName) }
                    SummaryRow("📖 Reading", "${profile.readingPace.displayName} — ${profile.readingPace.description}")
                    SummaryRow("📅 Notifications", "${profile.readingPace.timesPerDay}× per day")
                    SummaryRow("📕 Translation", profile.favoriteTranslation.abbreviation)

                    if (profile.recommendedCategories.isNotEmpty()) {
                        SummaryRow("✨ Focus", profile.recommendedCategories.take(3).joinToString(", ") { cat -> cat.displayName })
                    }
                }
            }

            Text("Your widget theme, notification schedule, and verse categories have all been auto-set based on your answers. You can customize anything in Settings.",
                style = MaterialTheme.typography.bodySmall, color = Color.White.copy(.7f),
                textAlign = TextAlign.Center)
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF614385))) {
            Icon(Icons.Default.Check, null)
            Spacer(Modifier.width(8.dp))
            Text("Start My Journey", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(.7f))
        Text(value, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 8.dp))
    }
}

// ═══════════════════════════════════════════════════════════════
// SHARED COMPONENTS
// ═══════════════════════════════════════════════════════════════
@Composable
private fun GradientPage(colors: List<Color>, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors))) {
        Column(Modifier.fillMaxSize(), content = content)
    }
}

@Composable
private fun PageHeader(title: String, subtitle: String, emoji: String) {
    Column(Modifier.padding(top = 56.dp, start = 28.dp, end = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(emoji, fontSize = 44.sp)
        Text(title, style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif,
            color = Color.White, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(.75f),
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun SelectionCard(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit, accentColor: Color) {
    Card(
        onClick = onClick, modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(.35f) else Color.White.copy(.12f)),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, Color.White.copy(.2f))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(.65f))
            }
            if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun PageButtons(
    onNext: () -> Unit, onSkip: () -> Unit,
    nextEnabled: Boolean = true, nextLabel: String = "Continue"
) {
    Column(Modifier.padding(horizontal = 28.dp).padding(bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onNext, enabled = nextEnabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF333333),
                disabledContainerColor = Color.White.copy(.3f), disabledContentColor = Color.White.copy(.5f))) {
            Text(nextLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.ArrowForward, null)
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("Skip", color = Color.White.copy(.65f))
        }
    }
}

// LazyVerticalGrid uses standard androidx.compose.foundation.lazy.grid.items

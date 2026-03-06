package com.scripturewidgets.presentation.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scripturewidgets.domain.model.*
import com.scripturewidgets.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val cfg           by viewModel.widgetConfig.collectAsState()
    val premium       by viewModel.hasPremium.collectAsState()
    val notifEnabled  by viewModel.notificationsEnabled.collectAsState()
    val schedules     by viewModel.notificationSchedules.collectAsState()
    val frequency     by viewModel.notificationFrequency.collectAsState()
    val notifCat      by viewModel.notificationCategory.collectAsState()
    val userName      by viewModel.userName.collectAsState()
    val streak        by viewModel.streakDays.collectAsState()
    val versesRead    by viewModel.totalVersesRead.collectAsState()
    val randomOnOpen  by viewModel.randomizeOnOpen.collectAsState()
    val darkTheme     by viewModel.appThemeDark.collectAsState()
    val defaultTrans  by viewModel.defaultTranslation.collectAsState()

    // Tab state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Widget", "Notifications", "Profile", "App")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Customize") })
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { idx, title ->
                        Tab(selected = selectedTab == idx, onClick = { selectedTab = idx },
                            text = { Text(title, style = MaterialTheme.typography.labelMedium) })
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> WidgetTab(cfg, premium, viewModel)
                1 -> NotificationsTab(notifEnabled, schedules, frequency, notifCat, premium, viewModel)
                2 -> ProfileTab(userName, streak, versesRead, randomOnOpen, defaultTrans, viewModel)
                3 -> AppTab(darkTheme, premium, viewModel)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// WIDGET TAB
// ══════════════════════════════════════════════════════════════════
@Composable
private fun WidgetTab(cfg: WidgetConfig, premium: Boolean, vm: SettingsViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Live Preview
        WidgetLivePreview(cfg)

        // Theme
        SettingsCard("Theme", Icons.Default.Palette) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)) {
                items(WidgetTheme.entries) { theme ->
                    ThemeChip(theme, cfg.theme == theme, theme.isPremium && !premium) {
                        if (!theme.isPremium || premium) vm.updateTheme(theme)
                    }
                }
            }
        }

        // Background Style
        SettingsCard("Background Style", Icons.Default.Layers) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(WidgetBackground.entries) { bg ->
                    val locked = bg.isPremium && !premium
                    FilterChip(
                        selected = cfg.background == bg,
                        onClick  = { if (!locked) vm.updateBackground(bg) },
                        label    = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (locked) Icon(Icons.Default.Lock, null, Modifier.size(12.dp))
                                Text(bg.displayName, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    )
                }
            }
        }

        // Font Style
        SettingsCard("Font Style", Icons.Default.TextFields) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(WidgetFontStyle.entries) { style ->
                    FilterChip(selected = cfg.fontStyle == style, onClick = { vm.updateFontStyle(style) },
                        label = { Text(style.displayName, fontFamily = when(style) {
                            WidgetFontStyle.SERIF -> FontFamily.Serif
                            WidgetFontStyle.SANS_SERIF -> FontFamily.SansSerif
                            WidgetFontStyle.ITALIC -> FontFamily.Serif
                            else -> FontFamily.Default
                        }) })
                }
            }
        }

        // Font Size
        SettingsCard("Font Size  ·  ${cfg.fontSize.toInt()}sp", Icons.Default.FormatSize) {
            Slider(value = cfg.fontSize, onValueChange = vm::updateFontSize, valueRange = 10f..22f, steps = 11,
                modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("10sp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(.5f))
                Text("22sp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(.5f))
            }
        }

        // Text Alignment
        SettingsCard("Text Alignment", Icons.Default.FormatAlignCenter) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WidgetTextAlign.entries.forEach { align ->
                    val icon = when(align) {
                        WidgetTextAlign.LEFT   -> Icons.Default.FormatAlignLeft
                        WidgetTextAlign.CENTER -> Icons.Default.FormatAlignCenter
                        WidgetTextAlign.RIGHT  -> Icons.Default.FormatAlignRight
                    }
                    FilterChip(selected = cfg.textAlign == align, onClick = { vm.updateTextAlign(align) },
                        label = { Icon(icon, align.displayName, Modifier.size(18.dp)) })
                }
            }
        }

        // Translation
        SettingsCard("Bible Translation", Icons.Default.Book) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BibleTranslation.entries) { t ->
                    FilterChip(selected = cfg.translation == t, onClick = { vm.updateTranslation(t) },
                        label = { Text(t.abbreviation) })
                }
            }
        }

        // Content Type
        SettingsCard("Widget Shows", Icons.Default.DynamicFeed) {
            WidgetContentType.entries.forEach { type ->
                Row(Modifier.fillMaxWidth().clickable { vm.updateContentType(type) }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = cfg.contentType == type, onClick = { vm.updateContentType(type) })
                    Spacer(Modifier.width(4.dp))
                    Text(type.displayName, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Category (only if relevant)
        AnimatedVisibility(cfg.contentType == WidgetContentType.VERSE_BY_CATEGORY,
            enter = expandVertically(), exit = shrinkVertically()) {
            SettingsCard("Verse Category", Icons.Default.Category) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VerseCategory.entries) { cat ->
                        FilterChip(selected = cfg.category == cat, onClick = { vm.updateCategory(cat) },
                            label = { Text(cat.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(cat.colorHex).copy(.85f),
                                selectedLabelColor = Color.White))
                    }
                }
            }
        }

        // Display Toggles
        SettingsCard("Display Options", Icons.Default.Tune) {
            ToggleRow("Show Scripture Reference", cfg.showReference, vm::toggleShowReference)
            ToggleRow("Show Translation (e.g. KJV)", cfg.showTranslation, vm::toggleShowTranslation)
            ToggleRow("Show Verse Numbers", cfg.showVerseNumber, vm::toggleShowVerseNumber)
            ToggleRow("Compact Mode", cfg.compactMode, vm::toggleCompactMode)
        }

        if (!premium) PremiumBanner { vm.setPremium(true) }
        Spacer(Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════════════════════════
// NOTIFICATIONS TAB
// ══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsTab(
    enabled:   Boolean,
    schedules: List<NotificationSchedule>,
    frequency: NotificationFrequency,
    notifCat:  VerseCategory,
    premium:   Boolean,
    vm:        SettingsViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── Master Toggle ─────────────────────────────────────────
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (enabled) "Notifications On" else "Notifications Off",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (enabled) "Receiving ${frequency.displayName.lowercase()}"
                        else "Tap to enable verse reminders",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(.75f)
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(.6f)
                    )
                }
                Switch(
                    checked  = enabled,
                    onCheckedChange = {
                        val hour = schedules.firstOrNull()?.hour ?: 8
                        vm.updateNotificationSettings(it, hour)
                        if (it) {
                            if (frequency != NotificationFrequency.CUSTOM)
                                com.scripturewidgets.worker.VerseNotificationWorker
                                    .scheduleByFrequency(context, frequency, notifCat)
                            else
                                com.scripturewidgets.worker.VerseNotificationWorker
                                    .scheduleAll(context, schedules)
                        } else {
                            com.scripturewidgets.worker.VerseNotificationWorker.cancelAll(context)
                        }
                    }
                )
            }
        }

        AnimatedVisibility(visible = enabled) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                // ── Frequency Picker ──────────────────────────────
                SettingsCard("How Often", Icons.Default.Schedule) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Choose how frequently you receive Bible verses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(.6f)
                        )
                        Spacer(Modifier.height(4.dp))
                        NotificationFrequency.entries.forEach { freq ->
                            FrequencyCard(
                                freq       = freq,
                                isSelected = frequency == freq,
                                onSelect   = {
                                    vm.updateNotificationSettings(
                                        true,
                                        schedules.firstOrNull()?.hour ?: 8,
                                        schedules.firstOrNull()?.minute ?: 0,
                                        freq
                                    )
                                    if (freq != NotificationFrequency.CUSTOM) {
                                        com.scripturewidgets.worker.VerseNotificationWorker
                                            .scheduleByFrequency(context, freq, notifCat)
                                    }
                                }
                            )
                        }
                    }
                }

                // ── Verse Category ────────────────────────────────
                SettingsCard("Verse Type", Icons.Default.AutoAwesome) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Which type of verses do you want in notifications?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(.6f)
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(VerseCategory.entries.size) { idx ->
                                val cat = VerseCategory.entries[idx]
                                FilterChip(
                                    selected = notifCat == cat,
                                    onClick  = {
                                        vm.updateNotificationSettings(
                                            true,
                                            schedules.firstOrNull()?.hour ?: 8,
                                            schedules.firstOrNull()?.minute ?: 0,
                                            frequency,
                                            cat
                                        )
                                    },
                                    label = { Text(cat.displayName, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(cat.colorHex).copy(.2f),
                                        selectedLabelColor     = Color(cat.colorHex)
                                    )
                                )
                            }
                        }
                    }
                }

                // ── Custom Schedule (only when CUSTOM mode) ────────
                AnimatedVisibility(visible = frequency == NotificationFrequency.CUSTOM) {
                    CustomScheduleSection(schedules, premium, vm)
                }

                // ── Preview card ──────────────────────────────────
                NotificationPreviewCard(frequency, notifCat)
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Frequency Option Card ─────────────────────────────────────────
@Composable
private fun FrequencyCard(freq: NotificationFrequency, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(.5f)
        ),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(freq.emoji, fontSize = 24.sp)
            Column(Modifier.weight(1f)) {
                Text(
                    freq.displayName,
                    style     = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color     = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    freq.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(.7f)
                            else MaterialTheme.colorScheme.onSurface.copy(.55f)
                )
            }
            if (freq.timesPerDay > 0) {
                Surface(shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(.15f)) {
                    Text(
                        "${freq.timesPerDay}×",
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style     = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color     = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface.copy(.6f)
                    )
                }
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ── Custom Schedule Section ───────────────────────────────────────
@Composable
private fun CustomScheduleSection(
    schedules: List<NotificationSchedule>,
    premium:   Boolean,
    vm:        SettingsViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    SettingsCard("Custom Times", Icons.Default.AccessTime) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Add specific times to receive a verse. Each slot can have a different category.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f)
            )
            Spacer(Modifier.height(4.dp))

            if (schedules.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No times added yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(.4f)
                    )
                }
            } else {
                schedules.forEach { schedule ->
                    ScheduleRow(
                        schedule = schedule,
                        onToggle = { enabled ->
                            vm.toggleSchedule(schedule.id, enabled)
                            if (enabled) com.scripturewidgets.worker.VerseNotificationWorker
                                .scheduleOne(context, schedule)
                            else com.scripturewidgets.worker.VerseNotificationWorker
                                .cancelOne(context, schedule.id)
                        },
                        onDelete = {
                            vm.removeNotificationSchedule(schedule.id)
                            com.scripturewidgets.worker.VerseNotificationWorker
                                .cancelOne(context, schedule.id)
                        }
                    )
                }
            }

            val canAdd = schedules.size < (if (premium) 10 else 3)
            OutlinedButton(
                onClick  = { showAddDialog = true },
                enabled  = canAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (canAdd) "Add Time"
                    else "Upgrade for more slots (${schedules.size}/${if (premium) 10 else 3})"
                )
            }
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            nextId   = (schedules.maxOfOrNull { it.id } ?: -1) + 1,
            onAdd    = { vm.addNotificationSchedule(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

// ── Schedule Row ──────────────────────────────────────────────────
@Composable
private fun ScheduleRow(
    schedule: NotificationSchedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(.4f)
        ),
        border = BorderStroke(
            1.dp,
            if (schedule.enabled) MaterialTheme.colorScheme.outlineVariant
            else MaterialTheme.colorScheme.outlineVariant.copy(.3f)
        )
    ) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Switch(
                checked         = schedule.enabled,
                onCheckedChange = onToggle,
                modifier        = Modifier.scale(0.85f)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "%02d:%02d".format(schedule.hour, schedule.minute),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = if (schedule.enabled) MaterialTheme.colorScheme.onSurface
                                 else MaterialTheme.colorScheme.onSurface.copy(.4f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(schedule.category.colorHex).copy(.15f)
                    ) {
                        Text(
                            schedule.category.displayName,
                            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(schedule.category.colorHex)
                        )
                    }
                    if (schedule.label.isNotBlank()) {
                        Text(
                            schedule.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(.5f)
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline, null,
                    tint     = MaterialTheme.colorScheme.error.copy(.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Notification Preview Card ─────────────────────────────────────
@Composable
private fun NotificationPreviewCard(frequency: NotificationFrequency, category: VerseCategory) {
    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Surface(shape = CircleShape, color = Color.White.copy(.15f), modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("✝", fontSize = 14.sp) }
                }
                Column {
                    Text("Scripture Widgets", style = MaterialTheme.typography.labelMedium,
                        color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("now", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(.5f))
                }
            }
            Text(
                frequency.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                "\u201CFor I know the plans I have for you...\u201D",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontStyle  = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Text(
                "\u2014 Jeremiah 29:11 (${category.displayName})",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(.6f)
            )
        }
    }
}

// ── Add Schedule Dialog ───────────────────────────────────────────
@Composable
private fun AddScheduleDialog(
    nextId:    Int,
    onAdd:     (NotificationSchedule) -> Unit,
    onDismiss: () -> Unit
) {
    var hour     by remember { mutableStateOf(8) }
    var minute   by remember { mutableStateOf(0) }
    var label    by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(VerseCategory.ALL) }

    val autoLabel = when (hour) {
        in 5..8   -> "Morning"
        in 9..11  -> "Mid-Morning"
        in 12..13 -> "Midday"
        in 14..16 -> "Afternoon"
        in 17..19 -> "Evening"
        in 20..21 -> "Night"
        else      -> "Late Night"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddAlarm, null, tint = MaterialTheme.colorScheme.primary)
                Text("Add Notification Time", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Time display
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "%02d:%02d".format(hour, minute),
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Hour slider
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hour", style = MaterialTheme.typography.labelMedium)
                        Text("%02d:00".format(hour), style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Slider(value = hour.toFloat(), onValueChange = { hour = it.toInt() },
                        valueRange = 0f..23f, steps = 22)
                }

                // Minute slider
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Minute", style = MaterialTheme.typography.labelMedium)
                        Text(":%02d".format(minute), style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Slider(value = minute.toFloat(), onValueChange = { minute = (it / 5).toInt() * 5 },
                        valueRange = 0f..55f, steps = 10)
                }

                // Optional label
                OutlinedTextField(
                    value       = label,
                    onValueChange = { label = it },
                    label       = { Text("Label (optional)") },
                    placeholder = { Text(autoLabel) },
                    singleLine  = true,
                    modifier    = Modifier.fillMaxWidth()
                )

                // Category
                Text("Verse Category", style = MaterialTheme.typography.labelMedium)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(VerseCategory.entries.size) { idx ->
                        val cat = VerseCategory.entries[idx]
                        FilterChip(
                            selected  = category == cat,
                            onClick   = { category = cat },
                            label     = { Text(cat.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors    = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(cat.colorHex).copy(.2f),
                                selectedLabelColor     = Color(cat.colorHex)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(NotificationSchedule(
                    id       = nextId,
                    hour     = hour,
                    minute   = minute,
                    enabled  = true,
                    category = category,
                    label    = label.ifBlank { autoLabel }
                ))
            }) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ═══════════════════════════════════════════════════════════════
// ══════════════════════════════════════════════════════════════════
// PROFILE TAB
// ══════════════════════════════════════════════════════════════════
@Composable
private fun ProfileTab(
    name: String, streak: Int, versesRead: Int,
    randomOnOpen: Boolean, defaultTrans: BibleTranslation,
    vm: SettingsViewModel
) {
    var editingName by remember { mutableStateOf(false) }
    var nameInput   by remember(name) { mutableStateOf(name) }
    val focusManager = LocalFocusManager.current

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Stats Card
        Card(Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.15f),
                        modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (name.isNotBlank()) name.first().uppercase() else "✝",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Column {
                        Text(name.ifBlank { "Scripture Reader" },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Member of the Word", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.7f))
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatPill("$streak", "Day Streak", Icons.Default.LocalFireDepartment)
                    StatPill("$versesRead", "Verses Read", Icons.Default.MenuBook)
                }
            }
        }

        // Name editor
        SettingsCard("Your Name", Icons.Default.Person) {
            if (editingName) {
                OutlinedTextField(value = nameInput, onValueChange = { nameInput = it },
                    label = { Text("Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        vm.saveUserName(nameInput); editingName = false; focusManager.clearFocus()
                    }),
                    trailingIcon = {
                        IconButton(onClick = { vm.saveUserName(nameInput); editingName = false; focusManager.clearFocus() }) {
                            Icon(Icons.Default.Check, "Save")
                        }
                    })
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(name.ifBlank { "Tap to set your name" }, modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (name.isBlank()) MaterialTheme.colorScheme.onSurface.copy(.4f) else MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { editingName = true }) { Icon(Icons.Default.Edit, "Edit") }
                }
            }
        }

        // Default translation
        SettingsCard("Default Bible Translation", Icons.Default.MenuBook) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BibleTranslation.entries) { t ->
                    FilterChip(selected = defaultTrans == t, onClick = { vm.setDefaultTranslation(t) },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(t.abbreviation, fontWeight = FontWeight.Bold)
                                Text(t.fullName, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(.6f))
                            }
                        })
                }
            }
        }

        // Reading preferences
        SettingsCard("Reading Preferences", Icons.Default.ImportContacts) {
            ToggleRow("Randomize verse on app open", randomOnOpen, vm::setRandomizeOnOpen,
                subtitle = "Show a different verse each time you open the app")
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatPill(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.7f))
    }
}

// ══════════════════════════════════════════════════════════════════
// APP TAB
// ══════════════════════════════════════════════════════════════════
@Composable
private fun AppTab(darkTheme: Boolean, premium: Boolean, vm: SettingsViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        SettingsCard("App Appearance", Icons.Default.DarkMode) {
            ToggleRow("Dark Mode", darkTheme, vm::setAppThemeDark)
        }

        SettingsCard("About Scripture Widgets", Icons.Default.Info) {
            InfoRow("Version", "1.0.0")
            InfoRow("Verses in library", "120+")
            InfoRow("Translations", "KJV, NIV, ESV, NLT, NASB")
            InfoRow("Widget sizes", "Small, Medium, Large")
        }

        if (!premium) PremiumBanner { vm.setPremium(true) }

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tip", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("Long-press your home screen → Widgets → Scripture Widgets to add a Bible verse widget.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════════════════════════
// SHARED COMPONENTS
// ══════════════════════════════════════════════════════════════════
@Composable
fun SettingsCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleSmall)
            }
            content()
        }
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onToggle: (Boolean) -> Unit, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f))
        }
        Switch(checked = value, onCheckedChange = onToggle)
    }
}

@Composable
private fun InfoRow(key: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(key, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(.6f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ThemeChip(theme: WidgetTheme, isSelected: Boolean, isLocked: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, tween(200), label = "border")
    Box(
        Modifier.size(width = 72.dp, height = 60.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(theme.startColorHex), Color(theme.endColorHex))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (isLocked) Icon(Icons.Default.Lock, null, tint = Color.White.copy(.8f), modifier = Modifier.size(14.dp))
            Text(theme.displayName, color = Color(theme.textColorHex), style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 4.dp))
        }
        if (isSelected) Surface(shape = CircleShape, color = Color.White.copy(.9f),
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(14.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, Modifier.size(10.dp), tint = Color(theme.startColorHex))
            }
        }
    }
}

@Composable
private fun WidgetLivePreview(cfg: WidgetConfig) {
    val startColor = Color(cfg.theme.startColorHex)
    val endColor   = Color(cfg.theme.endColorHex)
    val textColor  = Color(cfg.theme.textColorHex)
    val brush      = when (cfg.background) {
        WidgetBackground.SOLID, WidgetBackground.MINIMAL      -> Brush.linearGradient(listOf(startColor, startColor))
        WidgetBackground.MINIMAL_DARK                         -> Brush.linearGradient(listOf(Color(0xFF121212), Color(0xFF121212)))
        else                                                   -> Brush.linearGradient(listOf(startColor, endColor))
    }
    val alignment  = when (cfg.textAlign) {
        WidgetTextAlign.LEFT  -> TextAlign.Start
        WidgetTextAlign.RIGHT -> TextAlign.End
        else                  -> TextAlign.Center
    }
    val fontFamily = when (cfg.fontStyle) {
        WidgetFontStyle.SERIF, WidgetFontStyle.ITALIC, WidgetFontStyle.ELEGANT -> FontFamily.Serif
        WidgetFontStyle.SANS_SERIF, WidgetFontStyle.LIGHT                      -> FontFamily.SansSerif
        else                                                                    -> FontFamily.Default
    }
    val fontStyle  = if (cfg.fontStyle == WidgetFontStyle.ITALIC) FontStyle.Italic else FontStyle.Normal

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(6.dp)) {
        Box(Modifier.fillMaxWidth().background(brush).padding(20.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = when(cfg.textAlign) {
                WidgetTextAlign.LEFT -> Alignment.Start
                WidgetTextAlign.RIGHT -> Alignment.End
                else -> Alignment.CenterHorizontally
            }) {
                Text("Preview", style = MaterialTheme.typography.labelSmall, color = textColor.copy(.6f))
                Spacer(Modifier.height(6.dp))
                Text("\u201CFor God so loved the world\u2026\u201D",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = cfg.fontSize.sp),
                    fontFamily = fontFamily, fontStyle = fontStyle, color = textColor, textAlign = alignment)
                if (cfg.showReference) {
                    Spacer(Modifier.height(6.dp))
                    Text("John 3:16${if (cfg.showTranslation) " (${cfg.translation.abbreviation})" else ""}",
                        style = MaterialTheme.typography.labelSmall, color = textColor.copy(.8f), textAlign = alignment)
                }
            }
        }
    }
}

@Composable
fun PremiumBanner(onUnlock: () -> Unit) {
    Card(Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB8860B).copy(.12f)),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(.4f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Stars, null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f)) {
                Text("Unlock Premium", style = MaterialTheme.typography.titleSmall)
                Text("More themes, backgrounds & notification slots", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(.6f))
            }
            Button(onClick = onUnlock) { Text("Unlock") }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(Modifier)

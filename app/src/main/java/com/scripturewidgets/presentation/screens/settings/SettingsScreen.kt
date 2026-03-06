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
    enabled: Boolean,
    schedules: List<NotificationSchedule>,
    frequency: NotificationFrequency,
    notifCat: VerseCategory,
    premium: Boolean,
    vm: SettingsViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Master toggle
        SettingsCard("Verse Notifications", Icons.Default.NotificationsActive) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Enable Notifications", style = MaterialTheme.typography.bodyMedium)
                    Text("Get random Bible verses at your chosen times", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(.6f))
                }
                Switch(checked = enabled, onCheckedChange = {
                    val hour = schedules.firstOrNull()?.hour ?: 8
                    vm.updateNotificationSettings(it, hour)
                })
            }
        }

        AnimatedVisibility(enabled, enter = expandVertically(), exit = shrinkVertically()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Frequency
                SettingsCard("How Often", Icons.Default.Schedule) {
                    NotificationFrequency.entries.forEach { freq ->
                        Row(Modifier.fillMaxWidth().clickable {
                            vm.updateNotificationSettings(true, schedules.firstOrNull()?.hour ?: 8,
                                schedules.firstOrNull()?.minute ?: 0, freq)
                        }.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = frequency == freq, onClick = {
                                vm.updateNotificationSettings(true, schedules.firstOrNull()?.hour ?: 8,
                                    schedules.firstOrNull()?.minute ?: 0, freq)
                            })
                            Spacer(Modifier.width(4.dp))
                            Column {
                                Text(freq.displayName, style = MaterialTheme.typography.bodyMedium)
                                if (freq == NotificationFrequency.CUSTOM)
                                    Text("Set exact times below", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(.5f))
                            }
                        }
                    }
                }

                // Category for notifications
                SettingsCard("Verse Type for Notifications", Icons.Default.AutoAwesome) {
                    Text("Choose which verses to receive in notifications",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(.6f))
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(VerseCategory.entries) { cat ->
                            FilterChip(selected = notifCat == cat, onClick = {
                                vm.updateNotificationSettings(true, schedules.firstOrNull()?.hour ?: 8,
                                    category = cat)
                            }, label = { Text(cat.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(cat.colorHex).copy(.85f),
                                selectedLabelColor = Color.White))
                        }
                    }
                }

                // Schedule list
                SettingsCard("Notification Times", Icons.Default.AccessTime) {
                    Text("Add multiple times throughout your day",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(.6f))
                    Spacer(Modifier.height(8.dp))

                    schedules.forEach { schedule ->
                        ScheduleRow(
                            schedule  = schedule,
                            onToggle  = { vm.toggleSchedule(schedule.id, it) },
                            onDelete  = { vm.removeNotificationSchedule(schedule.id) }
                        )
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    }

                    val canAdd = schedules.size < (if (premium) 10 else 3)
                    OutlinedButton(
                        onClick  = { if (canAdd) showAddDialog = true },
                        enabled  = canAdd,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (canAdd) "Add Time" else "Upgrade for more times")
                    }
                }
            }
        }

        // Info card
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(.4f))) {
            Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("A random Bible verse is sent at each scheduled time. " +
                    "You can set a different verse category per schedule.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showAddDialog) {
        AddScheduleDialog(
            nextId   = (schedules.maxOfOrNull { it.id } ?: -1) + 1,
            onAdd    = { vm.addNotificationSchedule(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun ScheduleRow(schedule: NotificationSchedule, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Switch(checked = schedule.enabled, onCheckedChange = onToggle, modifier = Modifier.scale(0.85f))
        Column(Modifier.weight(1f)) {
            Text(
                text = "%02d:%02d".format(schedule.hour, schedule.minute),
                style = MaterialTheme.typography.titleMedium,
                color = if (schedule.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(.4f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(schedule.category.colorHex).copy(.15f)) {
                    Text(schedule.category.displayName, Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, color = Color(schedule.category.colorHex))
                }
                if (schedule.label.isNotBlank())
                    Text(schedule.label, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(.5f))
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.DeleteOutline, "Remove", tint = MaterialTheme.colorScheme.error.copy(.7f), modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleDialog(nextId: Int, onAdd: (NotificationSchedule) -> Unit, onDismiss: () -> Unit) {
    var hour     by remember { mutableStateOf(8) }
    var minute   by remember { mutableStateOf(0) }
    var label    by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(VerseCategory.ALL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Notification Time") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Choose when to receive a verse", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(.6f))

                // Hour / Minute pickers
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Hour", style = MaterialTheme.typography.labelMedium)
                        Slider(value = hour.toFloat(), onValueChange = { hour = it.toInt() },
                            valueRange = 0f..23f, steps = 22)
                        Text("%02d:00".format(hour), style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Minute", style = MaterialTheme.typography.labelMedium)
                        Slider(value = minute.toFloat(), onValueChange = { minute = (it.toInt() / 5) * 5 },
                            valueRange = 0f..55f, steps = 10)
                        Text(":%02d".format(minute), style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }

                // Label
                OutlinedTextField(value = label, onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    placeholder = { Text("e.g. Morning Devotion") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())

                // Category
                Text("Verse Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(VerseCategory.entries) { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat },
                            label = { Text(cat.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(cat.colorHex).copy(.85f),
                                selectedLabelColor = Color.White))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(NotificationSchedule(nextId, hour, minute, true, category,
                    label.ifBlank { getDefaultLabel(hour) }))
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun getDefaultLabel(hour: Int) = when (hour) {
    in 5..11  -> "Morning"
    in 12..13 -> "Midday"
    in 14..17 -> "Afternoon"
    in 18..21 -> "Evening"
    else      -> "Night"
}

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

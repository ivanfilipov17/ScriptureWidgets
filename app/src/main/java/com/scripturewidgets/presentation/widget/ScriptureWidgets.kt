package com.scripturewidgets.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.*
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.scripturewidgets.domain.model.BibleVerse
import com.scripturewidgets.domain.model.WidgetConfig
import com.scripturewidgets.domain.model.WidgetTheme

val verseTextKey        = stringPreferencesKey("verse_text")
val verseReferenceKey   = stringPreferencesKey("verse_reference")
val verseTranslationKey = stringPreferencesKey("verse_translation")
val themeNameKey        = stringPreferencesKey("theme_name")
val fontSizeKey         = floatPreferencesKey("widget_font_size")
val showReferenceKey    = booleanPreferencesKey("show_reference")

@Composable
fun ScriptureWidgetContent(
    verseText: String,
    verseReference: String,
    verseTranslation: String,
    theme: WidgetTheme,
    fontSize: Float,
    showReference: Boolean
) {
    val bgColor   = ColorProvider(Color(theme.startColorHex))
    val textColor = ColorProvider(Color(theme.textColorHex))
    val refColor  = ColorProvider(Color(theme.textColorHex).copy(alpha = 0.85f))

    Box(
        modifier = GlanceModifier.fillMaxSize().background(bgColor).padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\u201C$verseText\u201D",
                style = TextStyle(
                    color = textColor,
                    fontSize = fontSize.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.padding(horizontal = 4.dp),
                maxLines = 8
            )
            Spacer(GlanceModifier.height(8.dp))
            if (showReference) {
                Text(
                    text = "$verseReference ($verseTranslation)",
                    style = TextStyle(
                        color = refColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

class SmallScriptureWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val p = currentState<androidx.datastore.preferences.core.Preferences>()
            ScriptureWidgetContent(
                verseText        = p[verseTextKey] ?: "For God so loved the world\u2026",
                verseReference   = p[verseReferenceKey] ?: "John 3:16",
                verseTranslation = p[verseTranslationKey] ?: "NIV",
                theme            = safeTheme(p[themeNameKey]),
                fontSize         = p[fontSizeKey] ?: 12f,
                showReference    = p[showReferenceKey] ?: true
            )
        }
    }
}
class SmallScriptureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallScriptureWidget()
}

class MediumScriptureWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val p = currentState<androidx.datastore.preferences.core.Preferences>()
            ScriptureWidgetContent(
                verseText        = p[verseTextKey] ?: "For God so loved the world\u2026",
                verseReference   = p[verseReferenceKey] ?: "John 3:16",
                verseTranslation = p[verseTranslationKey] ?: "NIV",
                theme            = safeTheme(p[themeNameKey]),
                fontSize         = p[fontSizeKey] ?: 13f,
                showReference    = p[showReferenceKey] ?: true
            )
        }
    }
}
class MediumScriptureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumScriptureWidget()
}

class LargeScriptureWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val p = currentState<androidx.datastore.preferences.core.Preferences>()
            ScriptureWidgetContent(
                verseText        = p[verseTextKey] ?: "For God so loved the world\u2026",
                verseReference   = p[verseReferenceKey] ?: "John 3:16",
                verseTranslation = p[verseTranslationKey] ?: "NIV",
                theme            = safeTheme(p[themeNameKey]),
                fontSize         = p[fontSizeKey] ?: 14f,
                showReference    = p[showReferenceKey] ?: true
            )
        }
    }
}
class LargeScriptureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeScriptureWidget()
}

object ScriptureWidgetUpdater {
    suspend fun updateAll(context: Context, verse: BibleVerse, config: WidgetConfig) {
        SmallScriptureWidget().updateAll(context)
        MediumScriptureWidget().updateAll(context)
        LargeScriptureWidget().updateAll(context)
    }
}

private fun safeTheme(name: String?): WidgetTheme =
    name?.let { runCatching { WidgetTheme.valueOf(it) }.getOrNull() } ?: WidgetTheme.SUNRISE

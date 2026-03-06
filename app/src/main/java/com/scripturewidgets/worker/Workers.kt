package com.scripturewidgets.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.scripturewidgets.data.PreferencesRepository
import com.scripturewidgets.domain.model.NotificationFrequency
import com.scripturewidgets.domain.model.NotificationSchedule
import com.scripturewidgets.domain.model.VerseCategory
import com.scripturewidgets.domain.repository.VerseRepository
import com.scripturewidgets.presentation.MainActivity
import com.scripturewidgets.presentation.widget.ScriptureWidgetUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

// ── Verse Notification Worker ─────────────────────────────────────
// Fires at a specific scheduled time, picks a random verse, sends notification
@HiltWorker
class VerseNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val verseRepository: VerseRepository,
    private val preferences: PreferencesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val enabled = preferences.notificationsEnabled.first()
            if (!enabled) return Result.success()

            // Read category from inputData (each schedule can have its own category)
            val categoryName = inputData.getString(KEY_CATEGORY) ?: VerseCategory.ALL.name
            val category = runCatching { VerseCategory.valueOf(categoryName) }.getOrDefault(VerseCategory.ALL)

            val verse = verseRepository.getRandomVerse(category) ?: return Result.retry()
            val scheduleLabel = inputData.getString(KEY_LABEL) ?: "Scripture Widgets"

            sendVerseNotification(
                context     = context,
                verseText   = verse.text,
                reference   = verse.fullReference,
                notifId     = inputData.getInt(KEY_NOTIF_ID, NOTIFICATION_ID_BASE),
                channelId   = CHANNEL_ID,
                title       = scheduleLabel
            )
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME_PREFIX  = "verse_notif_"
        const val CHANNEL_ID        = "verse_notification_channel"
        const val NOTIFICATION_ID_BASE = 2000
        const val KEY_CATEGORY      = "category"
        const val KEY_LABEL         = "label"
        const val KEY_NOTIF_ID      = "notif_id"

        // Schedule a single notification for a given time today (or tomorrow if passed)
        fun scheduleOne(context: Context, schedule: NotificationSchedule) {
            if (!schedule.enabled) {
                cancelOne(context, schedule.id)
                return
            }
            val delay = calcDelayMs(schedule.hour, schedule.minute)
            val data  = workDataOf(
                KEY_CATEGORY to schedule.category.name,
                KEY_LABEL    to schedule.label.ifBlank { "Daily Verse" },
                KEY_NOTIF_ID to (NOTIFICATION_ID_BASE + schedule.id)
            )
            val request = PeriodicWorkRequestBuilder<VerseNotificationWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "$WORK_NAME_PREFIX${schedule.id}",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        // Schedule ALL notifications from the user's schedule list
        fun scheduleAll(context: Context, schedules: List<NotificationSchedule>) {
            schedules.forEach { scheduleOne(context, it) }
        }

        fun cancelOne(context: Context, scheduleId: Int) {
            WorkManager.getInstance(context).cancelUniqueWork("$WORK_NAME_PREFIX$scheduleId")
        }

        fun cancelAll(context: Context) {
            // Cancel up to 10 possible schedules
            (0..9).forEach { cancelOne(context, it) }
        }

        private fun calcDelayMs(hour: Int, minute: Int): Long {
            val now    = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}

// ── DailyVerseWorker (widget refresh) ────────────────────────────
@HiltWorker
class DailyVerseWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val verseRepository: VerseRepository,
    private val preferences: PreferencesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val verse  = verseRepository.getDailyVerse() ?: return Result.retry()
            val config = preferences.widgetConfig.first()
            ScriptureWidgetUpdater.updateAll(context, verse, config)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "daily_verse_work"
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyVerseWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(calcDelayToMidnight(), TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
        private fun calcDelayToMidnight(): Long {
            val now    = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 0)
                if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}

// ── Boot Receiver ─────────────────────────────────────────────────
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            DailyVerseWorker.schedule(context)
            // Note: notification schedules are re-scheduled via ScriptureApp on next launch
        }
    }
}

// ── Notification Helper ───────────────────────────────────────────
fun sendVerseNotification(
    context: Context,
    verseText: String,
    reference: String,
    notifId: Int = 2000,
    channelId: String = VerseNotificationWorker.CHANNEL_ID,
    title: String = "Daily Verse"
) {
    createNotificationChannel(context, channelId)
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pending = PendingIntent.getActivity(
        context, notifId, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_menu_info_details)
        .setContentTitle(title)
        .setContentText(reference)
        .setStyle(NotificationCompat.BigTextStyle().bigText("\u201C$verseText\u201D\n\u2014 $reference"))
        .setContentIntent(pending)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    try {
        NotificationManagerCompat.from(context).notify(notifId, notification)
    } catch (e: SecurityException) { /* permission not granted */ }
}

fun createNotificationChannel(context: Context, channelId: String = VerseNotificationWorker.CHANNEL_ID) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId, "Scripture Verses",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Bible verse notifications" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

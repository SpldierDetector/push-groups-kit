
package com.pushgroupskit

import android.os.Handler
import android.os.Looper

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.StatusBarNotification
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class PushGroupsKitModule : Module() {

  companion object {
    private const val CHANNEL_ID = "push_groups_kit_alerts_v3"
    private const val TAG_PREFIX = "push-groups-kit"
    private const val PRESS_ACTION = "com.pushgroupskit.NOTIFICATION_PRESSED"
    private const val EXTRA_GROUP_ID = "com.pushgroupskit.extra.GROUP_ID"
    private const val EXTRA_NOTIFICATION_ID = "com.pushgroupskit.extra.NOTIFICATION_ID"
  }

  @Suppress("DEPRECATION")
  private fun createBuilder(context: Context): Notification.Builder =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Notification.Builder(context, CHANNEL_ID)
    } else {
      Notification.Builder(context)
    }

  private fun createChannel(manager: NotificationManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Grouped notifications",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        enableVibration(true)
        setVibrationPattern(longArrayOf(0L, 200L, 120L, 200L))
      }

      manager.createNotificationChannel(channel)
    }
  }

  private fun getNotificationManager(): NotificationManager {
    val context = appContext.reactContext
      ?: error("Android context is unavailable")

    return context.getSystemService(
      Context.NOTIFICATION_SERVICE
    ) as NotificationManager
  }

  private fun getKitNotifications(
    manager: NotificationManager
  ): List<StatusBarNotification> {
    check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      "Notification management requires Android 6.0 or newer"
    }

    return manager.activeNotifications.filter { notification ->
      notification.tag?.startsWith("$TAG_PREFIX:") == true
    }
  }

  private fun createPressPendingIntent(
    context: Context,
    groupId: String,
    notificationId: String?
  ): PendingIntent {
    val intent = context.packageManager
      .getLaunchIntentForPackage(context.packageName)
      ?: error("Application launch activity was not found")

    intent.action = PRESS_ACTION

    intent.data = Uri.Builder()
      .scheme("pushgroupskit")
      .authority("notification")
      .appendPath(if (notificationId == null) "group" else "item")
      .appendPath(groupId)
      .apply {
        if (notificationId != null) {
          appendPath(notificationId)
        }
      }
      .build()

    intent.putExtra(EXTRA_GROUP_ID, groupId)

    if (notificationId != null) {
      intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId)
    }

    intent.addFlags(
      Intent.FLAG_ACTIVITY_CLEAR_TOP or
        Intent.FLAG_ACTIVITY_SINGLE_TOP
    )

    return PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or
        PendingIntent.FLAG_IMMUTABLE
    )
  }

  private fun readNotificationPress(
    intent: Intent?
  ): Map<String, Any?>? {
    if (intent?.action != PRESS_ACTION) {
      return null
    }

    val groupId = intent.getStringExtra(EXTRA_GROUP_ID)
      ?.takeIf { it.isNotBlank() }
      ?: return null

    return mapOf(
      "groupId" to groupId,
      "notificationId" to intent.getStringExtra(EXTRA_NOTIFICATION_ID)
    )
  }

  override fun definition() = ModuleDefinition {
    Name("PushGroupsKit")

    Events("onNotificationPressed")

    OnNewIntent { intent ->
      readNotificationPress(intent)?.let { press ->
        sendEvent("onNotificationPressed", press)
      }
    }

    AsyncFunction("getInitialNotificationPressAsync") {
      readNotificationPress(appContext.currentActivity?.intent)
    }

    AsyncFunction("getNotificationDiagnosticsAsync") {
      val context = appContext.reactContext
        ?: error("Android context is unavailable")

      val manager = context.getSystemService(
        Context.NOTIFICATION_SERVICE
      ) as NotificationManager

      val channel = if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
      ) {
        manager.getNotificationChannel(CHANNEL_ID)
      } else {
        null
      }

      mapOf(
        "channelId" to CHANNEL_ID,
        "notificationsEnabled" to manager.areNotificationsEnabled(),
        "importance" to channel?.importance,
        "vibrationEnabled" to channel?.shouldVibrate(),
        "sound" to channel?.sound?.toString()
      )
    }

    AsyncFunction("showUngroupedDiagnosticAsync") {
      val context = appContext.reactContext
        ?: error("Android context is unavailable")

      val manager = context.getSystemService(
        Context.NOTIFICATION_SERVICE
      ) as NotificationManager

      createChannel(manager)

      val notification = createBuilder(context)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Diagnostic notification")
        .setContentText("Testing vibration without grouping")
        .setAutoCancel(true)
        .build()

      Handler(Looper.getMainLooper()).postDelayed({
        manager.notify(
          "$TAG_PREFIX:diagnostic:${System.currentTimeMillis()}",
          0,
          notification
        )
      }, 5000L)

      Unit
    }

    AsyncFunction("showGroupedNotificationAsync") {
      notificationId: String,
      groupId: String,
      title: String,
      body: String ->

      require(notificationId.isNotBlank()) {
        "notificationId must not be empty"
      }

      require(groupId.isNotBlank()) {
        "groupId must not be empty"
      }

      val context = appContext.reactContext
        ?: error("Android context is unavailable")

      if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        context.checkSelfPermission(
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        error("Notification permission is not granted")
      }

      val manager = context.getSystemService(
        Context.NOTIFICATION_SERVICE
      ) as NotificationManager

      createChannel(manager)

      val groupKey = "$TAG_PREFIX:group:$groupId"

      val notification = createBuilder(context)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentIntent(createPressPendingIntent(context, groupId, notificationId))
        .setContentTitle(title)
        .setContentText(body)
        .setGroup(groupKey)
        .setAutoCancel(true)
        .build()

      manager.notify(
        "$TAG_PREFIX:item:$groupId:$notificationId",
        0,
        notification
      )

      val summaryBuilder = createBuilder(context)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentIntent(createPressPendingIntent(context, groupId, null))
        .setContentTitle(title)
        .setContentText("Grouped notifications")
        .setGroup(groupKey)
        .setGroupSummary(true)
        .setOnlyAlertOnce(true)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        summaryBuilder.setGroupAlertBehavior(
          Notification.GROUP_ALERT_CHILDREN
        )
      }

      manager.notify(
        "$TAG_PREFIX:summary:$groupId",
        0,
        summaryBuilder.build()
      )
    }

    AsyncFunction("dismissNotificationAsync") {
      notificationId: String,
      groupId: String ->

      require(notificationId.isNotBlank()) {
        "notificationId must not be empty"
      }

      require(groupId.isNotBlank()) {
        "groupId must not be empty"
      }

      val manager = getNotificationManager()

      val groupKey = "$TAG_PREFIX:group:$groupId"
      val itemTag = "$TAG_PREFIX:item:$groupId:$notificationId"
      val summaryTag = "$TAG_PREFIX:summary:$groupId"

      val hasOtherItems = getKitNotifications(manager).any {
        it.notification.group == groupKey &&
          it.tag?.startsWith("$TAG_PREFIX:item:") == true &&
          it.tag != itemTag
      }

      manager.cancel(itemTag, 0)

      if (!hasOtherItems) {
        manager.cancel(summaryTag, 0)
      }
    }

    AsyncFunction("dismissGroupAsync") { groupId: String ->
      require(groupId.isNotBlank()) {
        "groupId must not be empty"
      }

      val manager = getNotificationManager()
      val groupKey = "$TAG_PREFIX:group:$groupId"

      getKitNotifications(manager)
        .filter {
          it.notification.group == groupKey &&
            it.tag?.startsWith("$TAG_PREFIX:item:") == true
        }
        .forEach {
          manager.cancel(it.tag, it.id)
        }

      manager.cancel("$TAG_PREFIX:summary:$groupId", 0)
    }

    AsyncFunction("dismissAllAsync") {
      val manager = getNotificationManager()

      getKitNotifications(manager).forEach {
        manager.cancel(it.tag, it.id)
      }
    }
  }
}

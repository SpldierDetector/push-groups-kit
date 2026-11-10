package com.pushgroupskit

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class PushGroupsKitModule : Module() {

  companion object {
    private const val CHANNEL_ID = "push_groups_kit"
    private const val TAG_PREFIX = "push-groups-kit"
  }

  @Suppress("DEPRECATION")
  private fun createBuilder(
    context: Context
  ): Notification.Builder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Notification.BUilder(contex, CHANNEL_ID)
    } else {
      Notification.Builder(contex)
    }
  }

  private fu createChannel(
    manager: NotificationManager
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Grouped notifications",
        NotificationManager.IMPORTANCE_DEFAULT
      )

      manager.createNotificationChannel(channel)
    }
  }

  override fun definition() = ModuleDefinition {
    Name("PushGroupsKit")

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
  }
}

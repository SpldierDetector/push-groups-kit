
package com.pushgroupskit.test

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.pushgroupskit.PushGroupsKitAndroid
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class NativeNotificationTestModule : Module() {

  override fun definition() = ModuleDefinition {
    Name("NativeNotificationTest")

    AsyncFunction("scheduleAsync") {
      notificationId: String,
      groupId: String ->

      val context = appContext.reactContext
        ?.applicationContext
        ?: error("Android context is unavailable")

      val scheduled = Handler(Looper.getMainLooper())
        .postDelayed({
          try {
            PushGroupsKitAndroid.showGroupedNotification(
              context = context,
              notificationId = notificationId,
              groupId = groupId,
              title = "Alex",
              body = "Notification from native Kotlin"
            )
          } catch (error: Exception) {
            Log.e(
              "NativeNotificationTest",
              "Native notification failed",
              error
            )
          }
        }, 5000L)

      check(scheduled) {
        "Failed to schedule native notification"
      }

      Unit
    }
  }
}


package com.pushgroupskit

import android.content.Context
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record

class PushGroupsKitConfigRecord : Record {
  @Field
  val channelName: String? = null

  @Field
  val summaryText: String? = null

  @Field
  val smallIconName: String? = null
}

class PushGroupsKitModule : Module() {

  private fun getAndroidContext(): Context {
    return appContext.reactContext
      ?: error("Android context is unavailable")
  }

  override fun definition() = ModuleDefinition {
    Name("PushGroupsKit")

    Events("onNotificationPressed")

    OnNewIntent { intent ->
      PushGroupsKitAndroid
        .readNotificationPress(intent)
        ?.let { press ->
          sendEvent("onNotificationPressed", press)
        }
    }

    AsyncFunction("configureAsync") {
      config: PushGroupsKitConfigRecord ->

      PushGroupsKitAndroid.configure(
        context = getAndroidContext(),
        channelName = config.channelName,
        summaryText = config.summaryText,
        smallIconName = config.smallIconName
      )
    }

    AsyncFunction("getInitialNotificationPressAsync") {
      val activity = appContext.currentActivity
      val intent = activity?.intent

      val press =
        PushGroupsKitAndroid.readNotificationPress(intent)

      if (
        activity != null &&
        intent != null &&
        press != null
      ) {
        activity.setIntent(
          PushGroupsKitAndroid.clearNotificationPress(intent)
        )
      }

      press
    }

    AsyncFunction("getNotificationDiagnosticsAsync") {
      PushGroupsKitAndroid.getNotificationDiagnostics(
        getAndroidContext()
      )
    }

    AsyncFunction("showUngroupedDiagnosticAsync") {
      PushGroupsKitAndroid.showUngroupedDiagnostic(
        getAndroidContext()
      )
    }

    AsyncFunction("showGroupedNotificationAsync") {
      notificationId: String,
      groupId: String,
      title: String,
      body: String ->

      PushGroupsKitAndroid.showGroupedNotification(
        context = getAndroidContext(),
        notificationId = notificationId,
        groupId = groupId,
        title = title,
        body = body
      )
    }

    AsyncFunction("dismissNotificationAsync") {
      notificationId: String,
      groupId: String ->

      PushGroupsKitAndroid.dismissNotification(
        context = getAndroidContext(),
        notificationId = notificationId,
        groupId = groupId
      )
    }

    AsyncFunction("dismissGroupAsync") {
      groupId: String ->

      PushGroupsKitAndroid.dismissGroup(
        context = getAndroidContext(),
        groupId = groupId
      )
    }

    AsyncFunction("dismissAllAsync") {
      PushGroupsKitAndroid.dismissAll(
        getAndroidContext()
      )
    }
  }
}

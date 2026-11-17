
# Push Groups Kit

Android notification grouping for Expo and React Native applications.

![Version](https://img.shields.io/badge/version-0.2.0-blue)
![Platform](https://img.shields.io/badge/platform-Android-green)
![License](https://img.shields.io/badge/license-MIT-blue)

> Early development release. The library is functional but is not yet recommended for production use without additional testing.

## Overview

Push Groups Kit is an open-source native module that provides flexible Android notification grouping and management for Expo and React Native applications.

It allows developers to organize notifications using custom group identifiers without implementing native Android notification management from scratch.

The library is not limited to messaging applications. Groups can represent conversations, orders, projects, accounts, or other application-specific entities.

## Features

- Native Android notification grouping.
- Custom notification and group identifiers.
- Automatic group summary creation.
- Notification sound and vibration.
- Individual notification dismissal.
- Group notification dismissal.
- Dismissal of all library-managed notifications.
- Notification press events.
- Initial notification press handling.
- Configurable notification channel name.
- Configurable group summary text.
- Custom notification icons.
- Persistent notification configuration.
- Notification channel diagnostics.

## Platform Support

| Platform | Status |
| --- | --- |
| Android | Supported |
| iOS | Not supported |
| Web | Not supported |

### Tested environment

- Expo SDK 57
- React Native 0.86.3
- Android 14
- HONOR X7c (MagicOS 8.0)

Compatibility with other Android devices and Android versions requires additional testing.

## Requirements

- An Expo or React Native project with Expo Modules configured.
- A native Android build.
- Notification permission on Android 13 and newer.

Push Groups Kit contains native Android code and cannot run inside Expo Go.

Use an Expo development build or a standalone Android application.

## Installation

Download the package archive from the GitHub Releases page:

https://github.com/SpldierDetector/push-groups-kit/releases

Install the downloaded archive in your project:

```bash
npm install ./push-groups-kit-0.2.0.tgz
```

Rebuild your native Android application after installation.

## Basic Usage

```typescript
import PushGroupsKit from 'push-groups-kit';

await PushGroupsKit.showGroupedNotificationAsync(
  'message-001',
  'conversation-001',
  'Alex',
  'Hello!'
);

await PushGroupsKit.showGroupedNotificationAsync(
  'message-002',
  'conversation-001',
  'Alex',
  'How are you?'
);
```

Notifications with the same group ID are grouped together by Android.

## Notification Permissions

On Android 13 and newer, request notification permission before displaying notifications.

```typescript
import {
  PermissionsAndroid,
  Platform,
} from 'react-native';

if (
  Platform.OS === 'android' &&
  Number(Platform.Version) >= 33
) {
  await PermissionsAndroid.request(
    PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
  );
}
```

Make sure permission has been granted before calling the notification API.

## Configuration

```typescript
await PushGroupsKit.configureAsync({
  channelName: 'Messages',
  summaryText: 'New messages',
  smallIconName: 'ic_notification',
});
```

All configuration properties are optional.

| Property | Description | Default |
| --- | --- | --- |
| channelName | Android notification channel name | Grouped notifications |
| summaryText | Group summary content text | Grouped notifications |
| smallIconName | Android drawable resource name | System information icon |

Configuration is stored persistently on the device.

Properties omitted from subsequent configuration calls retain their previously saved values.

Android may independently control the appearance of group summaries. The configured summary text is not guaranteed to be visible on all devices.

## Custom Notification Icons

Custom icons must be available as Android drawable resources.

For example:

```text
android/app/src/main/res/drawable/ic_notification.xml
```

Configure the resource using its name without the file extension:

```typescript
await PushGroupsKit.configureAsync({
  smallIconName: 'ic_notification',
});
```

Use a monochrome notification icon with a transparent background.

For Expo projects using Continuous Native Generation, a config plugin can be used to generate or copy the drawable resource during Prebuild.

See the example application for a working config plugin.

If a supplied drawable resource cannot be found,
the configuration call rejects with an error.

## Notification Management

### Dismiss an individual notification

```typescript
await PushGroupsKit.dismissNotificationAsync(
  'message-001',
  'conversation-001'
);
```

### Dismiss a notification group

```typescript
await PushGroupsKit.dismissGroupAsync(
  'conversation-001'
);
```

### Dismiss all notifications

```typescript
await PushGroupsKit.dismissAllAsync();
```

Only notifications managed by Push Groups Kit are affected.

## Notification Press Handling

Subscribe to notification press events:

```typescript
const subscription = PushGroupsKit.addListener(
  'onNotificationPressed',
  (event) => {
    console.log(event.groupId);
    console.log(event.notificationId);
  }
);

// Remove the listener when it is no longer needed.
subscription.remove();
```

The event payload has the following structure:

```typescript
type NotificationPress = {
  groupId: string;
  notificationId: string | null;
};
```

When a group summary is pressed,
notificationId is null.

### Initial notification press

To retrieve notification press information when the application is launched from a notification:

```typescript
const press =
  await PushGroupsKit.getInitialNotificationPressAsync();

if (press) {
  console.log(press.groupId);
  console.log(press.notificationId);
}
```

Initial press information is consumed when retrieved.

Applications should handle both initial notification presses and live notification press events.

## API Reference

| Method | Return type |
| --- | --- |
| configureAsync(config) | Promise&lt;void&gt; |
| showGroupedNotificationAsync(notificationId, groupId, title, body) | Promise&lt;void&gt; |
| dismissNotificationAsync(notificationId, groupId) | Promise&lt;void&gt; |
| dismissGroupAsync(groupId) | Promise&lt;void&gt; |
| dismissAllAsync() | Promise&lt;void&gt; |
| getInitialNotificationPressAsync() | Promise&lt;NotificationPress or null&gt; |
| getNotificationDiagnosticsAsync() | Promise&lt;object&gt; |
| showUngroupedDiagnosticAsync() | Promise&lt;void&gt; |

### Events

| Event | Payload |
| --- | --- |
| onNotificationPressed | NotificationPress |

## Diagnostics

Inspect notification settings:

```typescript
const diagnostics =
  await PushGroupsKit.getNotificationDiagnosticsAsync();

console.log(diagnostics);
```

Available diagnostic properties:

- channelId
- notificationsEnabled
- importance
- vibrationEnabled
- sound

To test notification behavior without grouping:

```typescript
await PushGroupsKit.showUngroupedDiagnosticAsync();
```

The diagnostic notification is scheduled with a five-second delay.

## Limitations

- Android only.
- Expo Go is not supported.
- Remote push notification delivery is not included.
- Custom notification sounds are not currently supported.
- Notification appearance may vary across Android devices.
- Sound and vibration depend on system notification settings.

## Roadmap

- Integration testing with real applications.
- Extended Android device compatibility testing.
- Improved notification press lifecycle handling.
- Automated testing.
- Additional notification configuration options.
- Future iOS support evaluation.

## Contributing

Issues, bug reports, feature requests, and pull requests are welcome.

https://github.com/SpldierDetector/push-groups-kit/issues

## License

MIT License. See [LICENSE](LICENSE).

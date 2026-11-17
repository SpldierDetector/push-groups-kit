
# Changelog

All notable changes to Push Groups Kit will be documented in this file.

## [0.2.0] - 2026-11-17

### Added

- Individual notification dismissal API.
- Notification group dismissal API.
- Dismissal of all library-managed notifications.
- Notification press events.
- Initial notification press handling.
- Configurable notification channel name.
- Configurable group summary text.
- Custom Android notification icons.
- Persistent notification configuration.
- Example application notification configuration controls.
- Example Expo config plugin for custom notification icons.

### Fixed

- Repeated retrieval of consumed initial notification press data.
- Group summary cleanup after dismissing the last notification.

### Documentation

- Added installation instructions.
- Added public API reference.
- Added notification permission instructions.
- Added configuration and notification management examples.
- Documented platform support and current limitations.

### Testing

- Verified notification grouping.
- Verified notification dismissal.
- Verified notification sound and vibration.
- Verified notification press handling.
- Verified custom notification icons.
- Verified persistent notification configuration.
- Tested on HONOR X7c running Android 14.

## [0.1.0]

### Added

- Initial Expo native module implementation.
- Native Android notification grouping.
- Custom group and notification identifiers.
- Automatic group summary creation.
- Notification channel creation.
- Notification sound and vibration.
- Notification channel diagnostics.
- Ungrouped diagnostic notifications.
- Example application.
- MIT license.

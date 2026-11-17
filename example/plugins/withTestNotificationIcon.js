
const { withDangerousMod } = require('expo/config-plugins');
const fs = require('fs/promises');
const path = require('path');

module.exports = function withTestNotificationIcon(config) {
  return withDangerousMod(config, [
    'android',
    async (config) => {
      const drawableDir = path.join(
        config.modRequest.platformProjectRoot,
        'app/src/main/res/drawable'
      );

      await fs.mkdir(drawableDir, { recursive: true });

      const icon = `<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFFFF"
        android:pathData="M12,2 L15,9 L22,9 L17,14 L19,22 L12,17 L5,22 L7,14 L2,9 L9,9 Z" />
</vector>`;

      await fs.writeFile(
        path.join(drawableDir, 'ic_test_notification.xml'),
        icon,
        'utf8'
      );

      return config;
    },
  ]);
};

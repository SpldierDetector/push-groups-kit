import {
  NativeModule,
  requireNativeModule
} from 'expo';

declare class PushGroupsKitModule extends NativeModule<{}> {
  showGroupedNotificationAsync(
    notificationId: string,
    groupId: string,
    title: string,
    body: string
  ): Promise<void>;

  getNotificationDiagnosticsAsync(): Promise<{
    channelId: string;
    notificationsEnabled: boolean;
    importance: number | null;
    vibrratonEnabled: boolean | null;
    sound: string | null;
  }>;

  showUngroupedDiagnosticAsync(): Promise<void>;
}

export default requireNativeModule<PushGroupsKitModule>('PushGroupsKit');

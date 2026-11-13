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
    vibrationEnabled: boolean | null;
    sound: string | null;
  }>;

  dismissNotificationAsync(
    notificationId: string,
    groupId: string
  ): Promise<void>;

  dismissGroupAsync(groupId: string): Promise<void>;

  dismissAllAsync(): Promise<void>;

  showUngroupedDiagnosticAsync(): Promise<void>;
}

export default requireNativeModule<PushGroupsKitModule>('PushGroupsKit');

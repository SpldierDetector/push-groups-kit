import {
  NativeModule,
  requireNativeModule
} from 'expo';

export type NotificationPress = {
  groupId: string;
  notificationId: string | null;
};

export type PushGroupsKitConfig = {
  channelName?: string;
  summaryText?: string;
};

type PushGroupsKitModuleEvents = {
  onNotificationPressed: (event: NotificationPress) => void;
};

declare class PushGroupsKitModule extends NativeModule<PushGroupsKitModuleEvents> {
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

  configureAsync(config: PushGroupsKitConfig): Promise<void>;
  dismissGroupAsync(groupId: string): Promise<void>;
  dismissAllAsync(): Promise<void>;
  showUngroupedDiagnosticAsync(): Promise<void>;
  getInitialNotificationPressAsync(): Promise<NotificationPress | null>;
}

export default requireNativeModule<PushGroupsKitModule>('PushGroupsKit');

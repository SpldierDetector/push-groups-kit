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
}

export default requireNativeModule<PushGroupsKitModule>('PushGroupsKit');

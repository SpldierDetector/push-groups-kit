import { NativeModule, requireNativeModule } from 'expo';

declare class PushGroupsKitModule extends NativeModule<{}> {
  setValueAsync(value: string): Promise<void>;
}

export default requireNativeModule<PushGroupsKitModule>('PushGroupsKit');

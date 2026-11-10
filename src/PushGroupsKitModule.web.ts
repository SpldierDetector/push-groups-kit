import { registerWebModule, NativeModule } from 'expo';

// PushGroupsKitModule is not available on the web platform.
class PushGroupsKitModule extends NativeModule<{}> {}

export default registerWebModule(PushGroupsKitModule, 'PushGroupsKitModule');

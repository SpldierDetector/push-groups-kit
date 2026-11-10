package com.pushgroupskit

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class PushGroupsKitModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("PushGroupsKit")

    AsyncFunction("setValueAsync") { value: String ->
    }
  }
}

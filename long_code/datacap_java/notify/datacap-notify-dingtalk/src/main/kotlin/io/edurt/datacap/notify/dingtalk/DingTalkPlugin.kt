package io.edurt.datacap.notify.dingtalk

import io.edurt.datacap.plugin.Plugin
import io.edurt.datacap.plugin.PluginType

class DingTalkPlugin : Plugin()
{
    override fun getType(): PluginType
    {
        return PluginType.NOTIFY
    }
}

package io.edurt.datacap.fs.cos

import io.edurt.datacap.plugin.Plugin
import io.edurt.datacap.plugin.PluginType

class TencentCosFsPlugin : Plugin()
{
    override fun getType(): PluginType
    {
        return PluginType.FILESYSTEM
    }
}

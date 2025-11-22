package io.edurt.datacap.fs.alioss

import io.edurt.datacap.plugin.Plugin
import io.edurt.datacap.plugin.PluginType

class AliOssFsPlugin : Plugin()
{
    override fun getType(): PluginType
    {
        return PluginType.FILESYSTEM
    }
}

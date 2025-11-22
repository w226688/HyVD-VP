package io.edurt.datacap.fs.qiniu

import io.edurt.datacap.plugin.Plugin
import io.edurt.datacap.plugin.PluginType

class QiniuFsPlugin : Plugin()
{
    override fun getType(): PluginType
    {
        return PluginType.FILESYSTEM
    }
}

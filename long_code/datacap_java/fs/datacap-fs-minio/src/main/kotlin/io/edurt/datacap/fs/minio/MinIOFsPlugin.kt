package io.edurt.datacap.fs.minio

import io.edurt.datacap.plugin.Plugin
import io.edurt.datacap.plugin.PluginType

class MinIOFsPlugin : Plugin()
{
    override fun getType(): PluginType
    {
        return PluginType.FILESYSTEM
    }
}

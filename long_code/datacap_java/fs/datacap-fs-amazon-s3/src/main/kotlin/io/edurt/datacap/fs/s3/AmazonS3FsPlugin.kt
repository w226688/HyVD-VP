package io.edurt.datacap.fs.s3

import io.edurt.datacap.plugin.Plugin
import io.edurt.datacap.plugin.PluginType

class AmazonS3FsPlugin : Plugin()
{
    override fun getType(): PluginType
    {
        return PluginType.FILESYSTEM
    }
}

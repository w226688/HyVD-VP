package io.edurt.datacap.executor.local

import io.edurt.datacap.plugin.Plugin
import io.edurt.datacap.plugin.PluginType

class LocalExecutorPlugin : Plugin()
{
    override fun getType(): PluginType
    {
        return PluginType.EXECUTOR
    }
}

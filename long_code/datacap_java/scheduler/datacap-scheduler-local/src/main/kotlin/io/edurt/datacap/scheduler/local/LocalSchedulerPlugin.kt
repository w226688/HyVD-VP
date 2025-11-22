package io.edurt.datacap.scheduler.local

import io.edurt.datacap.plugin.Plugin
import io.edurt.datacap.plugin.PluginType

class LocalSchedulerPlugin : Plugin()
{
    override fun getType(): PluginType
    {
        return PluginType.SCHEDULER
    }
}

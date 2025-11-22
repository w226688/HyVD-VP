package io.edurt.datacap.executor.seatunnel;

import io.edurt.datacap.plugin.Plugin;
import io.edurt.datacap.plugin.PluginType;

public class SeatunnelExecutorPlugin
        extends Plugin
{
    @Override
    public PluginType getType()
    {
        return PluginType.EXECUTOR;
    }
}

package io.edurt.datacap.fs;

import io.edurt.datacap.plugin.Plugin;
import io.edurt.datacap.plugin.PluginType;

public class LocalFsPlugin
        extends Plugin
{
    @Override
    public PluginType getType()
    {
        return PluginType.FILESYSTEM;
    }
}

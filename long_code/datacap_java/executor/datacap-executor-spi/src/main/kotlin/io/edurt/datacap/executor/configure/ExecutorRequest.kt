package io.edurt.datacap.executor.configure

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.edurt.datacap.executor.common.RunEngine
import io.edurt.datacap.executor.common.RunMode
import io.edurt.datacap.executor.common.RunWay
import io.edurt.datacap.plugin.PluginManager

@SuppressFBWarnings(value = ["EI_EXPOSE_REP", "EI_EXPOSE_REP2"])
data class ExecutorRequest(
    var taskName: String,
    var userName: String,
    var input: ExecutorConfigure,
    var output: ExecutorConfigure,
    var executorHome: String? = null,
    var workHome: String? = null,
    var pluginManager: PluginManager? = null,
    var timeout: Long = 600,
    var runWay: RunWay = RunWay.LOCAL,
    var runMode: RunMode = RunMode.CLIENT,
    var startScript: String? = null,
    var runEngine: RunEngine = RunEngine.SPARK,
    var transform: ExecutorConfigure? = null
)
{
    constructor(
        workHome: String?,
        input: ExecutorConfigure,
        output: ExecutorConfigure
    ) : this("", "", input, output, null, workHome, null, 600, RunWay.LOCAL, RunMode.CLIENT)

    constructor(
        workHome: String?,
        input: ExecutorConfigure,
        output: ExecutorConfigure,
        transform: ExecutorConfigure?,
        runEngine: RunEngine = RunEngine.SPARK
    ) : this("", "", input, output, null, workHome, null, 600, RunWay.LOCAL, RunMode.CLIENT, null, runEngine, transform)

    constructor(
        workHome: String? = null,
        executorHome: String? = null,
        taskName: String,
        userName: String,
        input: ExecutorConfigure,
        output: ExecutorConfigure,
        runMode: RunMode = RunMode.CLIENT,
        runWay: RunWay = RunWay.LOCAL
    ) : this(taskName, userName, input, output, executorHome, workHome, null, 600, runWay, runMode)

    constructor(
        workHome: String? = null,
        executorHome: String? = null,
        taskName: String,
        userName: String,
        input: ExecutorConfigure,
        output: ExecutorConfigure,
        runMode: RunMode = RunMode.CLIENT,
        runWay: RunWay = RunWay.LOCAL,
        startScript: String?
    ) : this(taskName, userName, input, output, executorHome, workHome, null, 600, runWay, runMode, startScript)

    constructor(
        workHome: String? = null,
        executorHome: String? = null,
        taskName: String,
        userName: String,
        input: ExecutorConfigure,
        output: ExecutorConfigure,
        runMode: RunMode = RunMode.CLIENT,
        runWay: RunWay = RunWay.LOCAL,
        startScript: String?,
        runEngine: RunEngine = RunEngine.SPARK,
        transform: ExecutorConfigure? = null
    ) : this(taskName, userName, input, output, executorHome, workHome, null, 600, runWay, runMode, startScript, runEngine, transform)
}

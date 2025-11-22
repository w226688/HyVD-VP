package io.edurt.datacap.executor

import io.edurt.datacap.executor.configure.ExecutorRequest
import io.edurt.datacap.executor.configure.ExecutorResponse
import io.edurt.datacap.plugin.Service

interface ExecutorService : Service
{
    fun start(request: ExecutorRequest): ExecutorResponse

    fun stop(request: ExecutorRequest): ExecutorResponse
}

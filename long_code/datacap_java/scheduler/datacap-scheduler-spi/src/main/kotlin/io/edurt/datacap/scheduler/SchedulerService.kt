package io.edurt.datacap.scheduler

import io.edurt.datacap.plugin.Service

interface SchedulerService : Service
{
    fun initialize(request: SchedulerRequest): SchedulerResponse

    fun stop(request: SchedulerRequest): SchedulerResponse
}

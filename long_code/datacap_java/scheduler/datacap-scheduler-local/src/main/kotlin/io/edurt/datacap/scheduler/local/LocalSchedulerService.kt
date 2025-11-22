package io.edurt.datacap.scheduler.local

import io.edurt.datacap.scheduler.SchedulerRequest
import io.edurt.datacap.scheduler.SchedulerResponse
import io.edurt.datacap.scheduler.SchedulerService

class LocalSchedulerService : SchedulerService
{
    override fun initialize(request: SchedulerRequest): SchedulerResponse
    {
        return QuartzEndpoint.createJob(request)
    }

    override fun stop(request: SchedulerRequest): SchedulerResponse
    {
        return QuartzEndpoint.removeJob(request)
    }
}

package io.edurt.datacap.notify

import io.edurt.datacap.notify.model.NotifyRequest
import io.edurt.datacap.notify.model.NotifyResponse
import io.edurt.datacap.plugin.Service

interface NotifyService : Service
{
    fun send(request: NotifyRequest): NotifyResponse
}

package io.edurt.datacap.server.controller;

import io.edurt.datacap.service.entity.NotificationEntity;
import io.edurt.datacap.service.repository.NotificationRepository;
import io.edurt.datacap.service.service.NotificationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(value = "/api/v1/notification")
public class NotificationController
        extends BaseController<NotificationEntity>
{
    private final NotificationService service;
    private final NotificationRepository repository;

    public NotificationController(NotificationService service, NotificationRepository repository)
    {
        super(repository, service);
        this.service = service;
        this.repository = repository;
    }
}

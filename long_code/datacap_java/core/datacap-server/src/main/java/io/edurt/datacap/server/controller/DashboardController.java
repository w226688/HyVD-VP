package io.edurt.datacap.server.controller;

import io.edurt.datacap.service.entity.DashboardEntity;
import io.edurt.datacap.service.repository.DashboardRepository;
import io.edurt.datacap.service.service.DashboardService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(value = "/api/v1/dashboard")
public class DashboardController
        extends BaseController<DashboardEntity>
{
    private final DashboardRepository repository;
    private final DashboardService service;

    protected DashboardController(DashboardRepository repository, DashboardService service)
    {
        super(repository, service);
        this.repository = repository;
        this.service = service;
    }
}

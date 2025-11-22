package io.edurt.datacap.server.controller;

import io.edurt.datacap.common.response.CommonResponse;
import io.edurt.datacap.service.itransient.configuration.Configuration;
import io.edurt.datacap.service.service.ConfigureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(value = "/api/v1/configure")
public class ConfigureController
{
    private final ConfigureService service;

    public ConfigureController(ConfigureService service)
    {
        this.service = service;
    }

    @GetMapping(value = "/executor")
    public CommonResponse<Configuration> executor()
    {
        return service.getExecutor();
    }
}

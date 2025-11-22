package io.edurt.datacap.server.controller;

import io.edurt.datacap.common.response.CommonResponse;
import io.edurt.datacap.service.entity.WorkflowEntity;
import io.edurt.datacap.service.repository.WorkflowRepository;
import io.edurt.datacap.service.service.WorkflowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/workflow")
public class WorkflowController
        extends BaseController<WorkflowEntity>
{
    private final WorkflowService service;

    protected WorkflowController(WorkflowRepository repository, WorkflowService service)
    {
        super(repository, service);
        this.service = service;
    }

    @GetMapping(value = "/log/{code}")
    public CommonResponse<List<String>> log(@PathVariable String code)
    {
        return service.log(code);
    }

    @PutMapping(value = "stop/{code}")
    public CommonResponse<Boolean> stop(@PathVariable String code)
    {
        return service.stop(code);
    }

    @PutMapping(value = "restart/{code}")
    public CommonResponse<String> restart(@PathVariable String code)
    {
        return service.restart(code);
    }
}

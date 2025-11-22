package io.edurt.datacap.server.controller.admin;

import io.edurt.datacap.server.controller.BaseController;
import io.edurt.datacap.service.entity.MenuEntity;
import io.edurt.datacap.service.repository.admin.MenuRepository;
import io.edurt.datacap.service.service.MenuService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/menu")
public class MenuController
        extends BaseController<MenuEntity>
{
    private final MenuRepository repository;
    private final MenuService service;

    public MenuController(MenuRepository repository, MenuService service)
    {
        super(repository, service);
        this.repository = repository;
        this.service = service;
    }
}

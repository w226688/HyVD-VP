package io.edurt.datacap.server.controller.admin;

import io.edurt.datacap.common.response.CommonResponse;
import io.edurt.datacap.server.controller.BaseController;
import io.edurt.datacap.service.entity.MenuEntity;
import io.edurt.datacap.service.entity.RoleEntity;
import io.edurt.datacap.service.repository.RoleRepository;
import io.edurt.datacap.service.service.RoleService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/role")
public class RoleController
        extends BaseController<RoleEntity>
{
    private final RoleRepository repository;
    private final RoleService service;

    public RoleController(RoleRepository repository, RoleService service)
    {
        super(repository, service);
        this.repository = repository;
        this.service = service;
    }

    @RequestMapping(value = "{id}/menus", method = {RequestMethod.GET, RequestMethod.PUT})
    public CommonResponse<? extends Object> getMenusByRoleId(@PathVariable(value = "id") Long id,
            @RequestBody(required = false) Set<Long> nodes)
    {
        if (ObjectUtils.isEmpty(nodes)) {
            return service.getMenusByRoleId(id);
        }
        else {
            return this.repository.findById(id)
                    .map(item -> {
                        Set<MenuEntity> menus = (Set<MenuEntity>) nodes.stream()
                                .map(v -> MenuEntity.builder().id(v).build())
                                .collect(Collectors.toSet());
                        item.setMenus(menus);
                        return service.saveOrUpdate(repository, item);
                    })
                    .orElse(CommonResponse.failure(String.format("Role [ %s ] not found", id)));
        }
    }
}

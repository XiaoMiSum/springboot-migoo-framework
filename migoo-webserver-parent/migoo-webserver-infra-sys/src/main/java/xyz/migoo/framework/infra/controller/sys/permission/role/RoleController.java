package xyz.migoo.framework.infra.controller.sys.permission.role;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.enums.CommonStatusEnum;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.sys.permission.role.vo.*;
import xyz.migoo.framework.infra.convert.sys.RoleConvert;
import xyz.migoo.framework.infra.dal.dataobject.sys.Role;
import xyz.migoo.framework.infra.service.sys.permission.PermissionService;
import xyz.migoo.framework.infra.service.sys.permission.RoleService;

import java.util.List;
import java.util.Set;

import static xyz.migoo.framework.common.enums.NumberConstants.N_1;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @Resource
    private PermissionService permissionService;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public Result<PageResult<RoleRespVO>> getRolePage(RoleQueryReqVO req) {
        return Result.getSuccessful(RoleConvert.INSTANCE.convert(roleService.getPage(req)));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermission('system:role:add')")
    public Result<?> addRole(@RequestBody RoleAddReqVO reqVO) {
        roleService.verify(reqVO.getCode(), reqVO.getName(), null);
        roleService.add(RoleConvert.INSTANCE.convert(reqVO));
        return Result.getSuccessful();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    public Result<?> updateRole(@RequestBody RoleUpdateReqVO reqVO) {
        roleService.verify(reqVO.getCode(), reqVO.getName(), reqVO.getId());
        roleService.update(RoleConvert.INSTANCE.convert(reqVO));
        return Result.getSuccessful();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    public Result<?> getRole(@PathVariable("id") Long id) {
        return Result.getSuccessful(RoleConvert.INSTANCE.convert(roleService.get(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:role:remove')")
    public Result<?> removeRole(@PathVariable("id") Long id) {
        roleService.remove(id);
        return Result.getSuccessful();
    }

    @GetMapping("/simple")
    public Result<List<RoleSimpleRespVO>> getSimpleMenus() {
        // 获得角色列表，只要开启状态 且 过滤 超级管理员
        List<Role> list = roleService.getList(CommonStatusEnum.ENABLE.getStatus())
                .stream().filter(role -> role.getId() > N_1).toList();
        return Result.getSuccessful(RoleConvert.INSTANCE.convert(list));
    }

    @GetMapping("/{roleId}/menu")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-menu')")
    public Result<Set<Long>> getRoleMenus(@PathVariable("roleId") Long roleId) {
        return Result.getSuccessful(permissionService.getRoleMenuIds(roleId));
    }

    @PostMapping("/{roleId}/menu")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-menu')")
    public Result<?> assignRoleMenus(@PathVariable("roleId") Long roleId, @Valid @RequestBody PermissionAssignRoleMenuReqVO reqVO) {
        reqVO.setRoleId(roleId);
        permissionService.assignRoleMenu(reqVO.getRoleId(), reqVO.getMenuIds());
        return Result.getSuccessful(true);
    }
}

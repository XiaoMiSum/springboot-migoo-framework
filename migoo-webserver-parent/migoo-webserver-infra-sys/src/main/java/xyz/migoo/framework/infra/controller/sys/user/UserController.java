package xyz.migoo.framework.infra.controller.sys.user;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.enums.CommonStatusEnum;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.collection.CollectionUtils;
import xyz.migoo.framework.infra.controller.sys.user.vo.*;
import xyz.migoo.framework.infra.convert.sys.UserConvert;
import xyz.migoo.framework.infra.dal.dataobject.sys.Dept;
import xyz.migoo.framework.infra.dal.dataobject.sys.User;
import xyz.migoo.framework.infra.service.sys.dept.DeptService;
import xyz.migoo.framework.infra.service.sys.permission.PermissionService;
import xyz.migoo.framework.infra.service.sys.user.UserService;
import xyz.migoo.framework.security.core.LoginUser;
import xyz.migoo.framework.security.core.annotation.Authenticator;
import xyz.migoo.framework.security.core.annotation.CurrentUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static xyz.migoo.framework.common.enums.NumberConstants.N_1;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private DeptService deptService;
    @Resource
    private PermissionService permissionService;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public Result<PageResult<UserPageItemRespVO>> getUserPage(@CurrentUser LoginUser currentUser, UserQueryReqVO req) {
        // 获得用户分页列表
        PageResult<User> pageResult = userService.getPage(req);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return Result.getSuccessful(PageResult.empty());
        }
        // 获得拼接需要的数据
        Collection<Long> deptIds = CollectionUtils.convertList(pageResult.getList(), User::getDeptId);
        Map<Long, Dept> deptMap = deptService.getDeptMap(deptIds);
        // 拼接结果返回
        List<UserPageItemRespVO> userList = Lists.newArrayListWithCapacity(pageResult.getList().size());
        for (User user : pageResult.getList()) {
            if (!currentUser.getId().equals(N_1.longValue()) && user.getId().equals(N_1.longValue())) {
                continue;
            }
            UserPageItemRespVO respVO = UserConvert.INSTANCE.convert(user);
            respVO.setDept(UserConvert.INSTANCE.convert(deptMap.get(user.getDeptId())));
            userList.add(respVO);
        }
        return Result.getSuccessful(new PageResult<>(userList, pageResult.getTotal()));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermission('system:user:add')")
    public Result<?> addUser(@Valid @RequestBody UserAddReqVO user) {
        userService.verify(user.getUsername());
        userService.add(UserConvert.INSTANCE.convert(user));
        return Result.getSuccessful();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public Result<?> updateUser(@Valid @RequestBody UserUpdateReqVO user) {
        userService.update(UserConvert.INSTANCE.convert(user));
        return Result.getSuccessful();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public Result<?> getUser(@PathVariable("id") Long id) {
        return Result.getSuccessful(UserConvert.INSTANCE.convert(userService.get(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:user:remove')")
    public Result<?> removeUser(@PathVariable("id") Long id) {
        userService.remove(id);
        return Result.getSuccessful();
    }

    @GetMapping("/simple")
    public Result<List<UserSimpleRespVO>> getSimple() {
        // 获得用户列表，只要开启状态的
        List<User> list = userService.get(CommonStatusEnum.ENABLE.getStatus());
        return Result.getSuccessful(UserConvert.INSTANCE.convert(list));
    }

    @GetMapping("/{userId}/role")
    public Result<Set<Long>> getUserRoles(@PathVariable("userId") Long userId) {
        return Result.getSuccessful(permissionService.getUserRoleIs(userId));
    }

    @PostMapping("/role")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-user-role')")
    public Result<?> assignUserRole(@Valid @RequestBody PermissionAssignUserRoleReqVO reqVO) {
        permissionService.assignUserRole(reqVO.getUserId(), reqVO.getRoleIds());
        return Result.getSuccessful(true);
    }

    @PostMapping("/password")
    @PreAuthorize("@ss.hasPermission('system:user:reset-password')")
    public Result<?> resetPassword(@RequestBody @Valid UserPasswordReqVO reqVO) {
        userService.update(UserConvert.INSTANCE.convert(reqVO));
        return Result.getSuccessful(true);
    }

    @PostMapping("/{no}/authenticator")
    @Authenticator
    @PreAuthorize("@ss.hasPermission('member:merchant:reset-authenticator')")
    public Result<Boolean> resetAuthenticator(@PathVariable("no") Long no) {
        userService.resetAuthenticator(no);
        return Result.getSuccessful(true);
    }
}

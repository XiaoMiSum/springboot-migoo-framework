package xyz.migoo.framework.infra.controller.sys.permission.menu;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.enums.CommonStatusEnum;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.sys.permission.menu.vo.*;
import xyz.migoo.framework.infra.convert.sys.MenuConvert;
import xyz.migoo.framework.infra.dal.dataobject.sys.Menu;
import xyz.migoo.framework.infra.service.sys.permission.MenuService;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private MenuService menuService;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public Result<List<MenuRespVO>> getMenus(MenuQueryReqVO reqVO) {
        List<Menu> list = menuService.get(reqVO);
        list.sort(Comparator.comparing(Menu::getSort));
        return Result.getSuccessful(MenuConvert.INSTANCE.convert(list));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermission('system:menu:add')")
    public Result<?> createMenu(@Valid @RequestBody MenuAddReqVO reqVO) {
        menuService.add(MenuConvert.INSTANCE.convert(reqVO));
        return Result.getSuccessful();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('system:menu:update')")
    public Result<?> updateMenu(@Valid @RequestBody MenuUpdateReqVO reqVO) {
        menuService.update(MenuConvert.INSTANCE.convert(reqVO));
        return Result.getSuccessful();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:menu:remove')")
    public Result<?> deleteMenu(@PathVariable("id") Long id) {
        menuService.remove(id);
        return Result.getSuccessful();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:menu:update')")
    public Result<MenuRespVO> getMenu(@PathVariable("id") Long id) {
        return Result.getSuccessful(MenuConvert.INSTANCE.convert(menuService.get(id)));
    }

    @GetMapping("/simple")
    public Result<List<MenuSimpleRespVO>> getSimpleMenus() {
        // 获得菜单列表，只要开启状态的
        MenuQueryReqVO reqVO = new MenuQueryReqVO();
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<Menu> list = menuService.get(reqVO);
        // 排序后，返回给前端
        list.sort(Comparator.comparing(Menu::getSort));
        return Result.getSuccessful(MenuConvert.INSTANCE.convert0(list));
    }
}

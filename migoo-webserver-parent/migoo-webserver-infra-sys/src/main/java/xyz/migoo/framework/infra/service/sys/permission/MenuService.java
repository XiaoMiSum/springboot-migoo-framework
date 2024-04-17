package xyz.migoo.framework.infra.service.sys.permission;

import xyz.migoo.framework.infra.controller.sys.permission.menu.vo.MenuQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Menu;

import java.util.Collection;
import java.util.List;

public interface MenuService {

    /**
     * 初始化菜单的本地缓存
     */
    void initLocalCache();

    /**
     * 删除菜单
     *
     * @param id 菜单编号
     */
    void remove(Long id);

    /**
     * 获得所有菜单列表
     *
     * @return 菜单列表
     */
    List<Menu> get(MenuQueryReqVO reqVO);

    /**
     * 获得所有菜单列表
     *
     * @return 菜单列表
     */
    List<Menu> get();

    /**
     * 获得所有菜单，从缓存中
     * <p>
     * 任意参数为空时，则返回为空
     *
     * @param menuTypes     菜单类型数组
     * @param menusStatuses 菜单状态数组
     * @return 菜单列表
     */
    List<Menu> listMenusFromCache(Collection<Integer> menuTypes, Collection<Integer> menusStatuses);

    /**
     * 获得指定编号的菜单数组，从缓存中
     * <p>
     * 任意参数为空时，则返回为空
     *
     * @param menuIds       菜单编号数组
     * @param menuTypes     菜单类型数组
     * @param menusStatuses 菜单状态数组
     * @return 菜单数组
     */
    List<Menu> listMenusFromCache(Collection<Long> menuIds, Collection<Integer> menuTypes,
                                  Collection<Integer> menusStatuses);

    /**
     * 获得权限对应的菜单数组
     *
     * @param permission 权限标识
     * @return 数组
     */
    List<Menu> getMenuListByPermissionFromCache(String permission);

    /**
     * 获得菜单
     *
     * @param id 菜单编号
     * @return 菜单
     */
    Menu get(Long id);

    void add(Menu menu);

    void update(Menu menu);
}

package xyz.migoo.framework.infra.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    DIR(1), // 目录
    MENU(2), // 菜单
    BUTTON(3) // 按钮
    ;

    /**
     * 类型
     */
    private final Integer type;

}

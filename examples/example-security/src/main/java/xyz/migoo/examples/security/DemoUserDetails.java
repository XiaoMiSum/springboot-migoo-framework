package xyz.migoo.examples.security;

import xyz.migoo.framework.security.core.AuthUserDetails;

/**
 * 示例认证主体
 *
 * <p>{@link AuthUserDetails} 是框架的登录用户基类（含用户编号/姓名/密码/启用状态/权限等），
 * 应用按需继承扩展业务字段；泛型自引用为 {@code AuthUserDetails} 的流式 setter 提供类型安全。</p>
 *
 * @author xiaomi
 */
public class DemoUserDetails extends AuthUserDetails<DemoUserDetails, Long> {

    // 示例不扩展额外字段；生产项目可在此增加 deptId、tenantId 等业务属性
}

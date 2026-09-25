package xyz.migoo.framework.security.core.authentication;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import xyz.migoo.framework.security.core.AuthUserDetails;

/**
 * JWT token 机械操作接口
 * <p>
 * 职责: 创建、解析、验证 token，与业务逻辑无关
 * 默认实现: {@link xyz.migoo.framework.security.core.authentication.JJwtTokenProvider}
 *
 * @author xiaomi
 */
public interface JwtTokenProvider {

    /**
     * 创建 access token
     *
     * @param user 用户信息
     * @return JWT token 字符串
     */
    String createAccessToken(AuthUserDetails<?, ?> user);

    /**
     * 创建 refresh token
     *
     * @param user 用户信息
     * @return JWT token 字符串
     */
    String createRefreshToken(AuthUserDetails<?, ?> user);

    /**
     * 解析并校验 token（实现必须在此完成全部有效性校验）
     * <p>
     * 实现必须校验: 签名、过期时间；exp 缺失或已过期均视为无效。
     * token 无效时必须抛出 {@link JwtException}（或其子类），
     * 禁止返回未经校验的 Jwt —— 否则会绕过 verifyToken / refreshToken 的 401/400 防线。
     *
     * @param token JWT token 字符串
     * @return Jwt 对象，包含 claims 信息（已通过全部校验）
     * @throws JwtException token 无效（签名错误/过期/exp 缺失/格式非法）
     */
    Jwt parseToken(String token);

    /**
     * 验证 token 是否有效（{@link #parseToken(String)} 的布尔包装）
     * <p>
     * 校验通过返回 true；token 无效（{@link JwtException} 或非法入参）返回 false。
     *
     * @param token JWT token 字符串
     * @return 是否有效
     */
    boolean isTokenValid(String token);

    /**
     * 从 Jwt 提取 userId (统一存储为 String)
     * <p>
     * 应用层在 {@link UserDetailsBridge#loadByUserId} 中将 String 转回自己的 ID 类型
     *
     * @param jwt Jwt 对象
     * @return userId 字符串
     */
    String getUserIdFromToken(Jwt jwt);

}

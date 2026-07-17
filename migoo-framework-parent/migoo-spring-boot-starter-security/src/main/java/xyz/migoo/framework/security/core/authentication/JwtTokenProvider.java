package xyz.migoo.framework.security.core.authentication;

import org.springframework.security.oauth2.jwt.Jwt;
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
     * 解析 token (验证签名 + 过期时间)
     *
     * @param token JWT token 字符串
     * @return Jwt 对象，包含 claims 信息
     */
    Jwt parseToken(String token);

    /**
     * 验证 token 是否有效 (签名 + 未过期)
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

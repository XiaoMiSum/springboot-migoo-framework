package xyz.migoo.framework.security.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.security.config.SecurityProperties;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class JwtUtil {

    private SecurityProperties properties;

    private SecretKey secretKey;

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        JsonUtils.parseObject(JsonUtils.toJsonString(userDetails), new TypeReference<Map<String, Object>>() {
        });
        return createToken(claims, userDetails.getUsername(), properties.getToken().getTimeout().toMillis());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        JsonUtils.parseObject(JsonUtils.toJsonString(userDetails), new TypeReference<Map<String, Object>>() {
        });
        return createToken(claims, userDetails.getUsername(), properties.getToken().getRefreshTimeout().toMillis());
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .issuer("migoo")
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            var claims = (Claims) Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer("migoo")
                    .build()
                    .parse(token)
                    .getPayload();
            if (claims == null) {
                return false;
            }
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
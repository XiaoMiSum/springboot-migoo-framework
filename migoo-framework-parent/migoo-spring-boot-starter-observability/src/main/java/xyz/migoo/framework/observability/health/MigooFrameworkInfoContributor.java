package xyz.migoo.framework.observability.health;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 框架信息输出（{@code GET /actuator/info}）
 *
 * <p>输出两部分：</p>
 * <ul>
 *     <li>{@code version}：框架版本（构建时由 maven 资源过滤注入
 *     {@code build-version.properties}，取不到时为 {@code unknown}）；</li>
 *     <li>{@code modules}：按 classpath 探测到的 MiGoo 组件（标记类存在即视为已装配）。</li>
 * </ul>
 *
 * <p>探测不产生任何副作用，未引入的组件只是不出现在列表里。</p>
 *
 * @author xiaomi
 */
public class MigooFrameworkInfoContributor implements InfoContributor {

    /**
     * 版本号资源（构建时过滤 {@code ${migoo.framework.version}}）
     */
    private static final String VERSION_RESOURCE = "xyz/migoo/framework/observability/build-version.properties";

    /**
     * 各组件的标记类（组件名 -> classpath 中必存在的类）
     */
    private static final Map<String, String> MODULE_MARKERS = new LinkedHashMap<>();

    /**
     * 框架版本（启动时读取一次）
     */
    private static final String VERSION = resolveVersion();

    static {
        MODULE_MARKERS.put("common", "xyz.migoo.framework.common.pojo.Result");
        MODULE_MARKERS.put("web", "xyz.migoo.framework.web.core.util.ServletUtils");
        MODULE_MARKERS.put("security", "xyz.migoo.framework.security.core.authentication.DefaultJwtAuthenticator");
        MODULE_MARKERS.put("redis", "xyz.migoo.framework.redis.core.RedisKit");
        MODULE_MARKERS.put("mq", "xyz.migoo.framework.mq.core.RedisMQTemplate");
        MODULE_MARKERS.put("mybatis", "xyz.migoo.framework.mybatis.core.handler.DefaultFieldHandler");
        MODULE_MARKERS.put("websocket", "xyz.migoo.framework.websocket.core.MiGooWebSocketHandler");
        MODULE_MARKERS.put("observability", "xyz.migoo.framework.observability.config.MiGooObservabilityAutoConfiguration");
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("migoo", Map.of("version", VERSION, "modules", detectModules()));
    }

    /**
     * 探测 classpath 中已装配的组件
     *
     * @return 组件名列表（保持定义顺序）
     */
    static List<String> detectModules() {
        ClassLoader classLoader = MigooFrameworkInfoContributor.class.getClassLoader();
        List<String> modules = new ArrayList<>();
        MODULE_MARKERS.forEach((module, marker) -> {
            if (ClassUtils.isPresent(marker, classLoader)) {
                modules.add(module);
            }
        });
        return modules;
    }

    /**
     * 读取构建时注入的框架版本
     *
     * @return 版本号；资源缺失或未被过滤时为 {@code unknown}
     */
    private static String resolveVersion() {
        try (InputStream in = MigooFrameworkInfoContributor.class.getClassLoader()
                .getResourceAsStream(VERSION_RESOURCE)) {
            if (in == null) {
                return "unknown";
            }
            Properties properties = new Properties();
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            String version = properties.getProperty("framework.version");
            // IDE 直接以源码目录运行时资源未经过滤，形如 ${...} 视为未知
            if (version == null || version.isBlank() || version.startsWith("${")) {
                return "unknown";
            }
            return version;
        } catch (IOException ex) {
            return "unknown";
        }
    }

}

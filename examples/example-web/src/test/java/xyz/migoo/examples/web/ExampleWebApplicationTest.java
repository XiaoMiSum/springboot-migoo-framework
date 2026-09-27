package xyz.migoo.examples.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端冒烟测试：统一响应 / 全局异常 / CORS strict 模式
 * <p>
 * 注：示例工程按 demo 惯例使用 @SpringBootTest 做端到端验证；
 * 框架各 starter 模块仍遵守仓库「纯单元测试」约定。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExampleWebApplicationTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:3000";
    private static final String EVIL_ORIGIN = "https://evil.example.org";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloReturnsUnifiedResult() throws Exception {
        mockMvc.perform(get("/api/hello").param("name", "MiGoo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("common.success"))
                .andExpect(jsonPath("$.data").value("hello, MiGoo"));
    }

    @Test
    void pageReturnsPagedResult() throws Exception {
        mockMvc.perform(get("/api/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    void businessExceptionIsHandledGlobally() throws Exception {
        // 业务异常不抛 500，由 GlobalExceptionHandler 统一转为 Result.code
        mockMvc.perform(get("/api/boom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001000000))
                .andExpect(jsonPath("$.msg").value("demo.resource.not_found"));
    }

    @Test
    void strictCorsAllowsConfiguredOrigin() throws Exception {
        mockMvc.perform(get("/api/hello").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    void strictCorsRejectsUnknownOrigin() throws Exception {
        mockMvc.perform(get("/api/hello").header(HttpHeaders.ORIGIN, EVIL_ORIGIN))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void corsPreflightSucceedsForConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/hello")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, not("true")));
    }
}

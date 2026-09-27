package xyz.migoo.examples.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;

import java.util.List;

import static xyz.migoo.framework.common.exception.ServiceExceptionUtil.get;

/**
 * 最小可用接口演示：统一响应与全局异常
 *
 * <p>框架不做 HTTP 状态码映射：正常与业务异常均返回 HTTP 200，由响应体
 * {@code Result.code} 区分（200 成功 / 自定义业务码 / 401 / 403 / 500），
 * 前端统一按 {@code code} 分支处理。</p>
 *
 * @author xiaomi
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    /**
     * 业务错误码（约定 ≥ 1000000000，避免与框架内置码段冲突）
     */
    private static final ErrorCode DEMO_NOT_FOUND = ErrorCode.of(1001000000, "demo.resource.not_found");

    /**
     * 统一响应：GET /api/hello?name=MiGoo → {"code":200,"msg":"common.success","data":"hello, MiGoo"}
     */
    @GetMapping("/hello")
    public Result<String> hello(@RequestParam(defaultValue = "MiGoo") String name) {
        return Result.ok("hello, " + name);
    }

    /**
     * 分页响应：GET /api/page
     */
    @GetMapping("/page")
    public Result<PageResult<String>> page() {
        return Result.ok(new PageResult<>(List.of("first", "second"), 2L));
    }

    /**
     * 业务异常：GET /api/boom → GlobalExceptionHandler 兜底，
     * 返回 {"code":1001000000,"msg":"demo.resource.not_found"}
     */
    @GetMapping("/boom")
    public Result<Void> boom() {
        throw get(DEMO_NOT_FOUND);
    }
}

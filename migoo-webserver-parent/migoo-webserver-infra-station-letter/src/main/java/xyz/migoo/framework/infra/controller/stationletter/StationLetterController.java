package xyz.migoo.framework.infra.controller.stationletter;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.string.StrUtils;
import xyz.migoo.framework.infra.controller.stationletter.vo.StationLetterPageReqVO;
import xyz.migoo.framework.infra.controller.stationletter.vo.StationLetterPageRespVO;
import xyz.migoo.framework.infra.controller.stationletter.vo.StationLetterRespVO;
import xyz.migoo.framework.infra.controller.stationletter.vo.StationLetterUpdateReqVO;
import xyz.migoo.framework.infra.convert.stationletter.StationLetterConvert;
import xyz.migoo.framework.infra.service.StationLetterService;
import xyz.migoo.framework.security.core.LoginUser;
import xyz.migoo.framework.security.core.annotation.CurrentUser;

@RestController
@RequestMapping("/station-letter")
public class StationLetterController {

    @Resource
    private StationLetterService service;

    @GetMapping
    public Result<PageResult<StationLetterPageRespVO>> getPage(@CurrentUser LoginUser loginUser, StationLetterPageReqVO req) {
        req.setToUserId(loginUser.getId());
        var result = StationLetterConvert.INSTANCE.convert(service.getPage(req));
        return Result.getSuccessful(result);
    }

    @GetMapping("/{id}")
    public Result<StationLetterRespVO> getPage(@PathVariable("id") Long id) {
        var result = StationLetterConvert.INSTANCE.convert1(service.get(id));
        return Result.getSuccessful(result);
    }

    @PutMapping("read")
    public Result<?> read(@RequestBody StationLetterUpdateReqVO req) {
        service.read(req.getIds());
        return Result.getSuccessful();
    }

    @PutMapping("unread")
    public Result<?> unread(@RequestBody StationLetterUpdateReqVO req) {
        service.unread(req.getIds());
        return Result.getSuccessful();
    }

    @DeleteMapping
    public Result<StationLetterRespVO> remove(@RequestParam("ids") String ids) {
        service.remove(StrUtils.splitToLong(ids, ","));
        return Result.getSuccessful();
    }
}

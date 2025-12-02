package xyz.migoo.framework.infra.controller.area;

import cn.hutool.core.lang.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.object.BeanUtils;
import xyz.migoo.framework.infra.Area;
import xyz.migoo.framework.infra.controller.area.vo.AreaNodeRespVO;
import xyz.migoo.framework.infra.utils.AreaUtils;

import java.util.List;


@RestController
@RequestMapping("/infra/area")
@Validated
public class AreaController {

    @GetMapping("/tree")
    public Result<List<AreaNodeRespVO>> getAreaTree() {
        Area area = AreaUtils.getArea(Area.ID_CHINA);
        Assert.notNull(area, "获取不到中国");
        return Result.ok(BeanUtils.toBean(area.getChildren(), AreaNodeRespVO.class));
    }

}

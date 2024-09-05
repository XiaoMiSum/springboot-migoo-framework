package xyz.migoo.framework.infra.dal.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.stationletter.vo.StationLetterPageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.StationLetterDO;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

import java.util.Set;

@Mapper
public interface StationLetterMapper extends BaseMapperX<StationLetterDO> {

    default PageResult<StationLetterDO> selectPage(StationLetterPageReqVO req) {
        return selectPage(req, new LambdaQueryWrapperX<StationLetterDO>()
                .eq(StationLetterDO::getUnread, req.getUnread())
                .eqIfPresent(StationLetterDO::getCode, req.getCode())
                .eqIfPresent(StationLetterDO::getToUserId, req.getToUserId())
                .eqIfPresent(StationLetterDO::getToUserType, req.getToUserType())
                .eqIfPresent(StationLetterDO::getFromUserId, req.getFromUserId())
                .eqIfPresent(StationLetterDO::getFromUserType, req.getFromUserType())
                .orderByDesc(StationLetterDO::getId));
    }
}

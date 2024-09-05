package xyz.migoo.framework.infra.convert.stationletter;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.stationletter.vo.StationLetterPageRespVO;
import xyz.migoo.framework.infra.controller.stationletter.vo.StationLetterRespVO;
import xyz.migoo.framework.infra.dal.dataobject.StationLetterDO;

@Mapper
public interface StationLetterConvert {

    StationLetterConvert INSTANCE = Mappers.getMapper(StationLetterConvert.class);

    PageResult<StationLetterPageRespVO> convert(PageResult<StationLetterDO> bean);

    StationLetterPageRespVO convert(StationLetterDO bean);

    StationLetterRespVO convert1(StationLetterDO bean);
}

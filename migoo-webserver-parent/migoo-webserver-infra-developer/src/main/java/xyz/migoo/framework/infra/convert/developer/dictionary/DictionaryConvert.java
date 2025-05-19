package xyz.migoo.framework.infra.convert.developer.dictionary;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.dictionary.vo.*;
import xyz.migoo.framework.infra.dal.dataobject.developer.dictionary.DictionaryDO;
import xyz.migoo.framework.infra.dal.dataobject.developer.dictionary.DictionaryValueDO;

import java.util.List;

@Mapper
public interface DictionaryConvert {

    DictionaryConvert INSTANCE = Mappers.getMapper(DictionaryConvert.class);

    DictionaryDO convert(DictionaryAddReqVO bean);

    DictionaryDO convert(DictionaryUpdateReqVO bean);

    DictionaryRespVO convert(DictionaryDO bean);

    PageResult<DictionaryRespVO> convert(PageResult<DictionaryDO> beans);

    List<DictionaryRespVO> convert(List<DictionaryDO> beans);

    DictionaryValueDO convert(DictionaryValueAddReqVO bean);

    DictionaryValueDO convert(DictionaryValueUpdateReqVO bean);

    DictionaryValueRespVO convert(DictionaryValueDO bean);

    PageResult<DictionaryValueRespVO> convert2(PageResult<DictionaryValueDO> beans);

    List<DictionaryValueRespVO> convert2(List<DictionaryValueDO> beans);
}

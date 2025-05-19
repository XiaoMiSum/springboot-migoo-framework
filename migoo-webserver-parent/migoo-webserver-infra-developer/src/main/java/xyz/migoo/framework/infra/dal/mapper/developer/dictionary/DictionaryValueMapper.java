package xyz.migoo.framework.infra.dal.mapper.developer.dictionary;

import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.dictionary.vo.DictionaryValuePageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.dictionary.DictionaryValueDO;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

@Mapper
public interface DictionaryValueMapper extends BaseMapperX<DictionaryValueDO> {

    default PageResult<DictionaryValueDO> selectPage(DictionaryValuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DictionaryValueDO>()
                .eq(DictionaryValueDO::getDictCode, reqVO.getDictCode())
                .likeIfPresent(DictionaryValueDO::getLabel, reqVO.getLabel())
                .orderByAsc(DictionaryValueDO::getSort));
    }
}

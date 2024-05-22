package xyz.migoo.framework.infra.dal.mapper.developer.dictionary;

import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.dictionary.vo.DictionaryPageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.dictionary.DictionaryDO;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

@Mapper
public interface DictionaryMapper extends BaseMapperX<DictionaryDO> {

    default PageResult<DictionaryDO> selectPage(DictionaryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DictionaryDO>()
                .eqIfPresent(DictionaryDO::getKey, reqVO.getKey())
                .eqIfPresent(DictionaryDO::getName, reqVO.getName())
                .orderByDesc(DictionaryDO::getId));
    }

    default DictionaryDO selectByKey(String key) {
        return selectOne(DictionaryDO::getKey, key);
    }
}

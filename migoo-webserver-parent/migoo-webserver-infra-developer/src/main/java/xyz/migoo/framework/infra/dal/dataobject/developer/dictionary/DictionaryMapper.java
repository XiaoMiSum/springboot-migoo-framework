package xyz.migoo.framework.infra.dal.dataobject.developer.dictionary;

import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.dictionary.vo.DictionaryPageReqVO;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

@Mapper
public interface DictionaryMapper extends BaseMapperX<DictionaryDO> {

    default PageResult<DictionaryDO> selectPage(DictionaryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DictionaryDO>()
                .likeIfPresent(DictionaryDO::getCode, reqVO.getCode())
                .likeIfPresent(DictionaryDO::getName, reqVO.getName())
                .orderByDesc(DictionaryDO::getId));
    }

    default DictionaryDO selectByCode(String code) {
        return selectOne(DictionaryDO::getCode, code);
    }
}

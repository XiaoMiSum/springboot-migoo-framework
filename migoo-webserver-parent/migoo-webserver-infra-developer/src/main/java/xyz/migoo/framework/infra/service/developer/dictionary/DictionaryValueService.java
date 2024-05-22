package xyz.migoo.framework.infra.service.developer.dictionary;

import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.dictionary.vo.DictionaryValuePageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.dictionary.DictionaryValueDO;

import java.util.List;

public interface DictionaryValueService {

    PageResult<DictionaryValueDO> get(DictionaryValuePageReqVO req);

    List<DictionaryValueDO> get();

    void add(DictionaryValueDO bean);

    void update(DictionaryValueDO bean);

    void remove(Long id);
}

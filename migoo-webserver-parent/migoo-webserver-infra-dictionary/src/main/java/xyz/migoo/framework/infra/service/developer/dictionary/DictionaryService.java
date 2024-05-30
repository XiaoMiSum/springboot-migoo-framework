package xyz.migoo.framework.infra.service.developer.dictionary;

import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.dictionary.vo.DictionaryPageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.dictionary.DictionaryDO;

import java.util.List;

public interface DictionaryService {

    PageResult<DictionaryDO> get(DictionaryPageReqVO req);

    List<DictionaryDO> get();

    void add(DictionaryDO bean);

    void update(DictionaryDO bean);

    void verify(String code, Long id);

    void remove(Long id);
}

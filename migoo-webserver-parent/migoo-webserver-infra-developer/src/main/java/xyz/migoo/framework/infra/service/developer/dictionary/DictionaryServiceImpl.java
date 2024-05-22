package xyz.migoo.framework.infra.service.developer.dictionary;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.dictionary.vo.DictionaryPageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.dictionary.DictionaryDO;
import xyz.migoo.framework.infra.dal.mapper.developer.dictionary.DictionaryMapper;

import java.util.List;
import java.util.Objects;

import static xyz.migoo.framework.infra.enums.ErrorCodeConstants.DICTIONARY_KEY_EXISTS;

@Service
public class DictionaryServiceImpl implements DictionaryService {

    @Resource
    private DictionaryMapper mapper;

    @Override
    public PageResult<DictionaryDO> get(DictionaryPageReqVO req) {
        return mapper.selectPage(req);
    }

    @Override
    public List<DictionaryDO> get() {
        return mapper.selectList();
    }

    @Override
    public void add(DictionaryDO bean) {
        mapper.insert(bean);
    }

    @Override
    public void update(DictionaryDO bean) {
        mapper.updateById(bean);
    }

    @Override
    public void verify(String key, Long id) {
        DictionaryDO d = mapper.selectByKey(key);
        if (Objects.nonNull(d) & Objects.nonNull(id) && !Objects.equals(id, d.getId())) {
            throw ServiceExceptionUtil.get(DICTIONARY_KEY_EXISTS);
        }
        if (Objects.nonNull(d) & Objects.isNull(id)) {
            throw ServiceExceptionUtil.get(DICTIONARY_KEY_EXISTS);
        }
    }

    @Override
    public void remove(Long id) {
        mapper.deleteById(id);
    }
}

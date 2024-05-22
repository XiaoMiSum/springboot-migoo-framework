package xyz.migoo.framework.infra.service.developer.dictionary;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.dictionary.vo.DictionaryValuePageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.dictionary.DictionaryValueDO;
import xyz.migoo.framework.infra.dal.mapper.developer.dictionary.DictionaryValueMapper;

import java.util.List;

@Service
public class DictionaryValueServiceImpl implements DictionaryValueService {

    @Resource
    private DictionaryValueMapper mapper;

    @Override
    public PageResult<DictionaryValueDO> get(DictionaryValuePageReqVO req) {
        return mapper.selectPage(req);
    }

    @Override
    public List<DictionaryValueDO> get() {
        return mapper.selectList();
    }

    @Override
    public void add(DictionaryValueDO bean) {
        mapper.insert(bean);
    }

    @Override
    public void update(DictionaryValueDO bean) {
        mapper.updateById(bean);
    }

    @Override
    public void remove(Long id) {
        mapper.deleteById(id);
    }
}

package xyz.migoo.framework.infra.service.cvs;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSProviderPageQueryReqVO;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSProviderQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSProviderDO;
import xyz.migoo.framework.infra.dal.mapper.cvs.CVSProviderMapper;

import java.util.List;

@Service
public class CVSProviderServiceImpl implements CVSProviderService {

    @Resource
    private CVSProviderMapper mapper;

    @Override
    public PageResult<CVSProviderDO> getPage(CVSProviderPageQueryReqVO req) {
        return mapper.selectPage(req);
    }

    @Override
    public List<CVSProviderDO> getList(CVSProviderQueryReqVO req) {
        return mapper.selectList(req);
    }

    @Override
    public CVSProviderDO get(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void add(CVSProviderDO bean) {
        mapper.insert(bean);
    }

    @Override
    public void update(CVSProviderDO bean) {
        mapper.updateById(bean);
    }

    @Override
    public void remove(Long id) {
        mapper.deleteById(id);
    }
}

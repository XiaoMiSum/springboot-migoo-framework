package xyz.migoo.framework.infra.service.cvs;

import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSProviderPageQueryReqVO;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSProviderQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSProviderDO;

import java.util.List;

public interface CVSProviderService {

    PageResult<CVSProviderDO> getPage(CVSProviderPageQueryReqVO req);

    List<CVSProviderDO> getList(CVSProviderQueryReqVO req);

    CVSProviderDO get(Long id);

    void add(CVSProviderDO bean);

    void update(CVSProviderDO bean);

    void remove(Long id);
}

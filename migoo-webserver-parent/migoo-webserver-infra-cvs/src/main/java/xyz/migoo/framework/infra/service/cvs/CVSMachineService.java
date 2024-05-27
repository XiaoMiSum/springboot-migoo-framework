package xyz.migoo.framework.infra.service.cvs;

import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.cvs.core.client.dto.Option;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSMachinePageQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSMachineDO;

import java.util.List;

public interface CVSMachineService {

    PageResult<CVSMachineDO> getPage(CVSMachinePageQueryReqVO req);

    List<CVSMachineDO> getList();

    void sync();

    void update(CVSMachineDO bean);

    void remove(Long id);

    Result<?> option(Long id, Option option);
}

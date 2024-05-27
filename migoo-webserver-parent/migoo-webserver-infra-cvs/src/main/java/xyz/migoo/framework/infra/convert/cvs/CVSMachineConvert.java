package xyz.migoo.framework.infra.convert.cvs;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.cvs.core.client.dto.CVMachineInstanceRespDTO;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSMachinePageRespVO;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSMachineUpdateReqVO;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSMachineDO;

@Mapper
public interface CVSMachineConvert {

    CVSMachineConvert INSTANCE = Mappers.getMapper(CVSMachineConvert.class);

    PageResult<CVSMachinePageRespVO> convert(PageResult<CVSMachineDO> bean);

    CVSMachinePageRespVO convert(CVSMachineDO bean);

    CVSMachineDO convert(CVSMachineUpdateReqVO bean);

    default CVSMachineDO convert(CVMachineInstanceRespDTO bean, String account) {
        return convert0(bean).setAccount(account);
    }

    CVSMachineDO convert0(CVMachineInstanceRespDTO bean);
}

package xyz.migoo.framework.infra.dal.mapper.cvs;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSMachinePageQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSMachineDO;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

@Mapper
public interface CVSMachineMapper extends BaseMapperX<CVSMachineDO> {
    
    default PageResult<CVSMachineDO> selectPage(CVSMachinePageQueryReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CVSMachineDO>()
                .likeIfPresent(CVSMachineDO::getAccount, reqVO.getAccount())
                .likeIfPresent(CVSMachineDO::getHostname, reqVO.getHostname()));
    }

    default void save(CVSMachineDO reqVO) {
        if (update(reqVO, new LambdaQueryWrapperX<CVSMachineDO>()
                .eq(CVSMachineDO::getInstanceId, reqVO.getInstanceId())) < 1) {
            this.insert(reqVO);
        }
    }

    default void deleteByInStanceId(String instanceId) {
        delete(new LambdaQueryWrapperX<CVSMachineDO>()
                .eq(CVSMachineDO::getInstanceId, instanceId));
    }

    @Delete("""
            <script>
            delete from infra_cloud_service_server;
            </script>
            """)
    void deleteAll();
}

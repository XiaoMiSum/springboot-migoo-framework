package xyz.migoo.framework.cvs.core.client;

import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.cvs.core.client.dto.CVMachineInstanceRespDTO;

import java.util.List;

public interface CVSClient {

    /**
     * 获取指定区域的实例列表
     *
     * @param regionId 区域id
     * @return 实例列表
     */
    Result<List<CVMachineInstanceRespDTO>> getInstances(String regionId);

    /**
     * 获取指定区域的实例列表
     *
     * @param regionId    区域id
     * @param instanceIds 实例编号
     * @return 实例列表
     */
    Result<List<CVMachineInstanceRespDTO>> getInstances(String regionId, List<String> instanceIds);

    /**
     * 指定实例
     *
     * @param instanceId 实例id
     * @return 重启结果
     */
    Result<Boolean> start(String instanceId);

    /**
     * 指定实例
     *
     * @param instanceId 实例id
     * @return 重启结果
     */
    Result<Boolean> stop(String instanceId);

    /**
     * 重启指定实例
     *
     * @param instanceId 实例id
     * @return 重启结果
     */
    Result<Boolean> reboot(String instanceId);


}

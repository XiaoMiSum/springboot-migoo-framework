package xyz.migoo.framework.aliyun.core.client;

import xyz.migoo.framework.aliyun.core.client.dto.CloudServerInstanceRespDTO;
import xyz.migoo.framework.aliyun.core.client.dto.CloudServerPriceRespDTO;
import xyz.migoo.framework.common.pojo.Result;

import java.util.List;

public interface CloudServiceClient {

    /**
     * 获取指定区域的实例列表
     *
     * @param regionId 区域id
     * @return 实例列表
     */
    Result<List<CloudServerInstanceRespDTO>> getInstances(String regionId);

    /**
     * 获取指定实例的续费价格
     *
     * @param regionId   区域id
     * @param instanceId 实例id
     * @return 续费价格
     */
    Result<CloudServerPriceRespDTO> getRenewalPrice(String regionId, String instanceId);
}

package xyz.migoo.framework.cvs.core.client;

import cn.hutool.core.util.StrUtil;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.cvs.core.client.dto.CloudServerInstanceRespDTO;
import xyz.migoo.framework.cvs.core.enums.CloudServerType;
import xyz.migoo.framework.cvs.core.enums.CloudServiceProvide;
import xyz.migoo.framework.cvs.core.property.CloudServiceProperties;

import java.util.List;

import static xyz.migoo.framework.cvs.core.enums.CloudServerType.ECS;
import static xyz.migoo.framework.cvs.core.enums.CloudServerType.RDS;
import static xyz.migoo.framework.cvs.core.enums.CloudServiceProvide.ALI_CLOUD;
import static xyz.migoo.framework.cvs.core.enums.CloudServiceProvide.TENCENT;

public interface CloudServiceClient {


    static CloudServiceClient create(CloudServiceProvide provide,
                                     CloudServerType serverType,
                                     CloudServiceProperties properties) {
        if (provide == ALI_CLOUD && serverType == ECS) {
            return new xyz.migoo.framework.cvs.core.client.impl.aliyun.ECSClient(properties);
        } else if (provide == ALI_CLOUD && serverType == RDS) {
            return new xyz.migoo.framework.cvs.core.client.impl.aliyun.RDSClient(properties);
        } else if (provide == TENCENT && serverType == ECS) {
            return new xyz.migoo.framework.cvs.core.client.impl.tencent.ECSClient(properties);
        } else if (provide == TENCENT && serverType == RDS) {
            return new xyz.migoo.framework.cvs.core.client.impl.tencent.RDSClient(properties);
        }
        throw ServiceExceptionUtil.get(new ErrorCode(-1,
                StrUtil.format("无可用云服务商客户端: {}, {}", provide.name(), serverType.name())));
    }

    /**
     * 获取指定区域的实例列表
     *
     * @param regionId 区域id
     * @return 实例列表
     */
    Result<List<CloudServerInstanceRespDTO>> getInstances(String regionId);

    /**
     * 获取指定区域的实例列表
     *
     * @param regionId    区域id
     * @param instanceIds 实例编号
     * @return 实例列表
     */
    Result<List<CloudServerInstanceRespDTO>> getInstances(String regionId, List<String> instanceIds);

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

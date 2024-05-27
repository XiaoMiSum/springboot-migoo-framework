package xyz.migoo.framework.cvs.core.client.aliyun;

import cn.hutool.core.date.DateUtil;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.ecs20140526.AsyncClient;
import com.aliyun.sdk.service.ecs20140526.models.*;
import com.google.common.collect.Lists;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.cvs.core.client.AbstractCVSClient;
import xyz.migoo.framework.cvs.core.client.dto.CVMachineInstanceRespDTO;
import xyz.migoo.framework.cvs.core.client.dto.InstanceStatus;
import xyz.migoo.framework.cvs.core.enums.CVSMachineType;
import xyz.migoo.framework.cvs.core.property.CVSClientProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ECSClient extends AbstractCVSClient {

    private AsyncClient client;

    public ECSClient(CVSClientProperties properties) {
        super(properties);
    }

    @Override
    protected void doInitialization() {
        try (StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(properties.getAccessKeyId())
                .accessKeySecret(properties.getAccessKeySecret())
                .build())) {
            client = AsyncClient.builder()
                    .region(properties.getRegion())
                    .credentialsProvider(provider)
                    .overrideConfiguration(ClientOverrideConfiguration.create()
                            .setEndpointOverride("ecs." + properties.getRegion() + ".aliyuncs.com"))
                    .build();
        }
    }

    @Override
    public Result<List<CVMachineInstanceRespDTO>> getInstances(String regionId) {
        return getInstances(regionId, null);
    }

    @Override
    public Result<List<CVMachineInstanceRespDTO>> getInstances(String regionId, List<String> instanceIds) {
        DescribeInstancesRequest describeInstancesRequest = DescribeInstancesRequest.builder()
                .regionId(regionId)
                .instanceIds(Objects.isNull(instanceIds) ? null : JsonUtils.toJsonString(instanceIds))
                .pageNumber(1)
                .pageSize(100)
                .build();
        List<CVMachineInstanceRespDTO> instances = Lists.newArrayList();
        try {
            DescribeInstancesResponse response = client.describeInstances(describeInstancesRequest).get();
            for (DescribeInstancesResponseBody.Instance instance : response.getBody().getInstances().getInstance()) {
                CVMachineInstanceRespDTO.CVMachineInstanceRespDTOBuilder builder = CVMachineInstanceRespDTO.builder()
                        .instanceId(instance.getInstanceId())
                        .hostname(instance.getHostName())
                        .status(InstanceStatus.valueOf(instance.getStatus()))
                        .operateSystem(instance.getOSName())
                        .publicIpAddress(Objects.isNull(instance.getPublicIpAddress()) ||
                                Objects.isNull(instance.getPublicIpAddress().getIpAddress()) ? null :
                                instance.getPublicIpAddress().getIpAddress().getFirst())
                        .privateIpAddress(Objects.isNull(instance.getInnerIpAddress()) ||
                                Objects.isNull(instance.getInnerIpAddress().getIpAddress()) ? null :
                                instance.getInnerIpAddress().getIpAddress().getFirst())
                        .machineType(CVSMachineType.ECS)
                        .createdTime(DateUtil.format(DateUtil.parse(instance.getCreationTime(), "yyyy-MM-ddTHH:mmZ"), "yyyy-MM-dd HH:mm:ss"))
                        .expiredTime(DateUtil.format(DateUtil.parse(instance.getExpiredTime(), "yyyy-MM-ddTHH:mmZ"), "yyyy-MM-dd HH:mm:ss"));
                if (!Objects.equals(InstanceStatus.valOf(instance.getStatus()), InstanceStatus.Released)) {
                    DescribeRenewalPriceRequest describeRenewalPriceRequest = DescribeRenewalPriceRequest.builder()
                            .regionId(regionId)
                            .resourceType("instance")
                            .resourceId(instance.getInstanceId())
                            .build();
                    DescribeRenewalPriceResponse price = client.describeRenewalPrice(describeRenewalPriceRequest).get();
                    builder.price(new BigDecimal(price.getBody().getPriceInfo().getPrice().getTradePrice().toString()));
                }
                instances.add(builder.build());
            }
            return Result.getSuccessful(instances);
        } catch (Exception e) {
            log.error("获取阿里云ecs服务器异常.", e);
            return Result.getError(-1, "获取阿里云ecs服务器异常.");
        }
    }

    @Override
    public Result<Boolean> start(String instanceId) {
        StartInstanceRequest startInstanceRequest = StartInstanceRequest.builder()
                .instanceId(instanceId)
                .build();
        try {
            client.startInstance(startInstanceRequest);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("启动阿里云ecs服务器异常.", e);
            return Result.getError(-1, "启动阿里云ecs服务器异常. " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> stop(String instanceId) {
        StopInstanceRequest stopInstanceRequest = StopInstanceRequest.builder()
                .instanceId(instanceId)
                .forceStop(true)
                .build();
        try {
            client.stopInstance(stopInstanceRequest);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("关闭阿里云ecs服务器异常.", e);
            return Result.getError(-1, "关闭阿里云ecs服务器异常. " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> reboot(String instanceId) {
        RebootInstanceRequest rebootInstanceRequest = RebootInstanceRequest.builder()
                .instanceId(instanceId)
                .forceStop(true)
                .build();
        try {
            client.rebootInstance(rebootInstanceRequest);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("重启阿里云ecs服务器异常.", e);
            return Result.getError(-1, "重启阿里云ecs服务器异常. " + e.getMessage());
        }
    }
}

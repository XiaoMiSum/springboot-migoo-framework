package xyz.migoo.framework.cvs.core.client.aliyun;

import cn.hutool.core.date.DateUtil;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.rds20140815.AsyncClient;
import com.aliyun.sdk.service.rds20140815.models.*;
import com.google.common.collect.Lists;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.cvs.core.client.AbstractCVSClient;
import xyz.migoo.framework.cvs.core.client.dto.CVMachineInstanceRespDTO;
import xyz.migoo.framework.cvs.core.client.dto.InstanceStatus;
import xyz.migoo.framework.cvs.core.enums.CVSMachineType;
import xyz.migoo.framework.cvs.core.property.CVSClientProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
public class RDSClient extends AbstractCVSClient {

    private AsyncClient client;

    public RDSClient(CVSClientProperties properties) {
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
                            .setEndpointOverride("rds." + properties.getRegion() + ".aliyuncs.com"))
                    .build();
        }
    }

    @Override
    public Result<List<CVMachineInstanceRespDTO>> getInstances(String regionId) {
        return Result.getSuccessful(_instances(regionId, null));
    }

    @Override
    public Result<List<CVMachineInstanceRespDTO>> getInstances(String regionId, List<String> instanceIds) {
        List<CVMachineInstanceRespDTO> instances = Lists.newArrayList();
        instanceIds.forEach(item -> instances.addAll(_instances(regionId, item)));
        return Result.getSuccessful(instances);
    }

    @Override
    public Result<Boolean> start(String instanceId) {
        StartDBInstanceRequest startInstanceRequest = StartDBInstanceRequest.builder()
                .DBInstanceId(instanceId)
                .build();
        try {
            client.startDBInstance(startInstanceRequest);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("启动阿里云rds服务器异常.", e);
            return Result.getError(-1, "启动阿里云rds服务器异常. " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> stop(String instanceId) {
        StopDBInstanceRequest stopInstanceRequest = StopDBInstanceRequest.builder()
                .DBInstanceId(instanceId)
                .build();
        try {
            client.stopDBInstance(stopInstanceRequest);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("关闭阿里云rds服务器异常.", e);
            return Result.getError(-1, "关闭阿里云rds服务器异常. " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> reboot(String instanceId) {
        RestartDBInstanceRequest rebootInstanceRequest = RestartDBInstanceRequest.builder()
                .DBInstanceId(instanceId)
                .build();
        try {
            client.restartDBInstance(rebootInstanceRequest);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("重启阿里云rds服务器异常.", e);
            return Result.getError(-1, "重启阿里云rds服务器异常. " + e.getMessage());
        }
    }

    private List<CVMachineInstanceRespDTO> _instances(String regionId, String instanceId) {
        DescribeDBInstancesRequest describeInstancesRequest = DescribeDBInstancesRequest.builder()
                .regionId(regionId)
                .DBInstanceId(instanceId)
                .pageNumber(1)
                .pageSize(100)
                .build();
        List<CVMachineInstanceRespDTO> instances = Lists.newArrayList();
        try {
            DescribeDBInstancesResponse response = client.describeDBInstances(describeInstancesRequest).get();
            for (DescribeDBInstancesResponseBody.DBInstance instance : response.getBody().getItems().getDBInstance()) {
                CVMachineInstanceRespDTO.CVMachineInstanceRespDTOBuilder builder = CVMachineInstanceRespDTO.builder()
                        .instanceId(instance.getDBInstanceId())
                        .hostname(instance.getEngine())
                        .status(InstanceStatus.valueOf(instance.getDBInstanceStatus()))
                        .operateSystem(instance.getEngine() + " " + instance.getEngineVersion())
                        .publicIpAddress(instance.getConnectionString())
                        .machineType(CVSMachineType.RDS)
                        .createdTime(DateUtil.format(DateUtil.parse(instance.getCreateTime(), "yyyy-MM-ddTHH:mmZ"), "yyyy-MM-dd HH:mm:ss"))
                        .expiredTime(DateUtil.format(DateUtil.parse(instance.getExpireTime(), "yyyy-MM-ddTHH:mmZ"), "yyyy-MM-dd HH:mm:ss"));
                if (!Objects.equals(InstanceStatus.valueOf(instance.getDBInstanceStatus()), InstanceStatus.Released)) {
                    DescribeRenewalPriceRequest describeRenewalPriceRequest = DescribeRenewalPriceRequest.builder()
                            .regionId(regionId)
                            .DBInstanceId(instance.getDBInstanceId())
                            .usedTime(1)
                            .timeType("Month")
                            .build();
                    DescribeRenewalPriceResponse price = client.describeRenewalPrice(describeRenewalPriceRequest).get();
                    builder.price(new BigDecimal(price.getBody().getPriceInfo().getTradePrice().toString()));
                }

                instances.add(builder.build());
            }
        } catch (Exception e) {
            log.error("获取阿里云ecs服务器异常.", e);
        }
        return instances;
    }
}

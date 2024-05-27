package xyz.migoo.framework.cvs.core.client.tencent;

import com.google.common.collect.Lists;
import com.tencentcloudapi.cdb.v20170320.CdbClient;
import com.tencentcloudapi.cdb.v20170320.models.*;
import com.tencentcloudapi.common.Credential;
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

    private CdbClient client;

    public RDSClient(CVSClientProperties properties) {
        super(properties);
    }

    @Override
    protected void doInitialization() {
        Credential cred = new Credential(properties.getAccessKeyId(), properties.getAccessKeySecret());
        client = new CdbClient(cred, properties.getRegion());
    }

    @Override
    public Result<List<CVMachineInstanceRespDTO>> getInstances(String regionId) {
        return getInstances(regionId, null);
    }

    @Override
    public Result<List<CVMachineInstanceRespDTO>> getInstances(String regionId, List<String> instanceIds) {
        DescribeDBInstancesRequest request = new DescribeDBInstancesRequest();
        request.setLimit(2000L);
        if (Objects.nonNull(instanceIds)) {
            request.setInstanceIds(instanceIds.toArray(new String[0]));
        }
        List<CVMachineInstanceRespDTO> instances = Lists.newArrayList();
        try {
            DescribeDBInstancesResponse resp = client.DescribeDBInstances(request);
            for (InstanceInfo item : resp.getItems()) {
                CVMachineInstanceRespDTO.CVMachineInstanceRespDTOBuilder builder = CVMachineInstanceRespDTO.builder()
                        .instanceId(item.getInstanceId())
                        .hostname(item.getInstanceName())
                        .status(InstanceStatus.valOf(item.getStatus().intValue()))
                        .operateSystem("MySql " + item.getEngineVersion())
                        .publicIpAddress(item.getWanDomain() + ":" + item.getWanPort())
                        .machineType(CVSMachineType.RDS)
                        .createdTime(item.getCreateTime())
                        .expiredTime(item.getDeadlineTime());
                if (!Objects.equals(InstanceStatus.valOf(item.getStatus().intValue()), InstanceStatus.Released)) {
                    DescribeDBPriceRequest describeDBPriceRequest = new DescribeDBPriceRequest();
                    describeDBPriceRequest.setInstanceId(item.getInstanceId());
                    DescribeDBPriceResponse price = client.DescribeDBPrice(describeDBPriceRequest);
                    builder.price(new BigDecimal(price.getPrice()));
                }
                instances.add(builder.build());
            }
        } catch (Exception e) {
            log.error("获取腾讯云RDS失败. ", e);
        }
        return Result.getSuccessful(instances);
    }

    @Override
    public Result<Boolean> start(String instanceId) {
        return Result.getError(-1, "腾讯云RDS不支持此操作. ");
    }

    @Override
    public Result<Boolean> stop(String instanceId) {
        return Result.getError(-1, "腾讯云RDS不支持此操作. ");
    }

    @Override
    public Result<Boolean> reboot(String instanceId) {
        RestartDBInstancesRequest request = new RestartDBInstancesRequest();
        request.setInstanceIds(new String[]{instanceId});
        try {
            client.RestartDBInstances(request);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("重启腾讯云RDS失败. ", e);
            return Result.getError(-1, "重启腾讯云RDS失败. " + e.getMessage());
        }
    }
}

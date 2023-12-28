package xyz.migoo.framework.cvs.core.client.impl.tencent;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.Lists;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.*;
import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.cvs.core.client.AbstractCloudServerClient;
import xyz.migoo.framework.cvs.core.client.dto.CloudServerInstanceRespDTO;
import xyz.migoo.framework.cvs.core.client.dto.InstanceStatus;
import xyz.migoo.framework.cvs.core.enums.CloudServerType;
import xyz.migoo.framework.cvs.core.property.CloudServiceProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ECSClient extends AbstractCloudServerClient {

    private CvmClient client;

    public ECSClient(CloudServiceProperties properties) {
        super(properties);
    }

    @Override
    protected void initialization(CloudServiceProperties properties) {
        Credential cred = new Credential(properties.getAccessKeyId(), properties.getAccessKeySecret());
        client = new CvmClient(cred, properties.getRegion());
    }

    @Override
    public Result<List<CloudServerInstanceRespDTO>> getInstances(String regionId) {
        return getInstances(regionId, null);
    }

    @Override
    public Result<List<CloudServerInstanceRespDTO>> getInstances(String regionId, List<String> instanceIds) {
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.setLimit(2000L);
        if (Objects.nonNull(instanceIds)) {
            request.setInstanceIds(instanceIds.toArray(new String[0]));
        }
        List<CloudServerInstanceRespDTO> instances = Lists.newArrayList();
        try {
            DescribeInstancesResponse resp = client.DescribeInstances(request);
            for (Instance item : resp.getInstanceSet()) {
                CloudServerInstanceRespDTO.CloudServerInstanceRespDTOBuilder builder = CloudServerInstanceRespDTO.builder()
                        .instanceId(item.getInstanceId())
                        .hostname(item.getInstanceName())
                        .status(InstanceStatus.valOf(item.getInstanceState()))
                        .operateSystem(item.getOsName())
                        .publicIpAddress(
                                ArrayUtil.isEmpty(item.getPublicIpAddresses()) ? "" : item.getPublicIpAddresses()[0]
                        )
                        .privateIpAddress(
                                ArrayUtil.isEmpty(item.getPrivateIpAddresses()) ? "" : item.getPrivateIpAddresses()[0]
                        )
                        .type(CloudServerType.ECS)
                        .createdTime(DateUtil.format(DateUtil.parse(item.getCreatedTime(), "yyyy-MM-ddTHH:mmZ"), "yyyy-MM-dd HH:mm:ss"))
                        .expiredTime(DateUtil.format(DateUtil.parse(item.getExpiredTime(), "yyyy-MM-ddTHH:mmZ"), "yyyy-MM-dd HH:mm:ss"));
                if (!Objects.equals(InstanceStatus.valOf(item.getInstanceState()), InstanceStatus.Released)) {
                    InquiryPriceRenewInstancesRequest priceRenewInstancesRequest = new InquiryPriceRenewInstancesRequest();
                    priceRenewInstancesRequest.setInstanceIds(new String[]{item.getInstanceId()});
                    InquiryPriceRenewInstancesResponse price = client.InquiryPriceRenewInstances(priceRenewInstancesRequest);
                    builder.price(new BigDecimal(price.getPrice().getInstancePrice().getDiscountPrice().toString()));
                }
                instances.add(builder.build());
            }
        } catch (Exception e) {
            log.error("获取腾讯云ECS失败. ", e);
        }
        return Result.getSuccessful(instances);
    }

    @Override
    public Result<Boolean> start(String instanceId) {
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
        startInstancesRequest.setInstanceIds(new String[]{instanceId});
        try {
            client.StartInstances(startInstancesRequest);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("启动腾讯云ECS失败. ", e);
            return Result.getError(-1, "启动腾讯云ECS失败. " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> stop(String instanceId) {
        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
        stopInstancesRequest.setInstanceIds(new String[]{instanceId});
        try {
            client.StopInstances(stopInstancesRequest);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("关闭腾讯云ECS失败. ", e);
            return Result.getError(-1, "关闭腾讯云ECS失败. " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> reboot(String instanceId) {
        RebootInstancesRequest rebootInstancesRequest = new RebootInstancesRequest();
        rebootInstancesRequest.setInstanceIds(new String[]{instanceId});
        try {
            client.RebootInstances(rebootInstancesRequest);
            return Result.getSuccessful(true);
        } catch (Exception e) {
            log.error("重启腾讯云ECS失败. ", e);
            return Result.getError(-1, "重启腾讯云ECS失败. " + e.getMessage());
        }
    }
}

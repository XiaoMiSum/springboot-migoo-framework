package xyz.migoo.framework.aliyun.core.client.impl;

import cn.hutool.core.date.DateUtil;
import com.aliyun.ecs20140526.Client;
import com.aliyun.ecs20140526.models.DescribeInstancesRequest;
import com.aliyun.ecs20140526.models.DescribeInstancesResponse;
import com.aliyun.ecs20140526.models.DescribeRenewalPriceRequest;
import com.aliyun.teaopenapi.models.Config;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.aliyun.core.client.AbstractCloudServerClient;
import xyz.migoo.framework.aliyun.core.client.dto.CloudServerInstanceRespDTO;
import xyz.migoo.framework.aliyun.core.client.dto.CloudServerPriceRespDTO;
import xyz.migoo.framework.aliyun.core.property.CloudServiceProperties;
import xyz.migoo.framework.common.pojo.Result;

import java.math.BigDecimal;
import java.util.List;

import static xyz.migoo.framework.aliyun.core.enums.CloudServerType.ECS;

@Slf4j
public class AliCloudServiceECSClient extends AbstractCloudServerClient {

    private Client client;

    public AliCloudServiceECSClient(CloudServiceProperties properties) throws Exception {
        super(properties);
    }

    @Override
    protected void init() throws Exception {
        client = new Client(new Config()
                .setAccessKeyId(properties.getAccessKeyId())
                .setAccessKeySecret(properties.getAccessKeySecret())
                .setEndpoint(properties.getEndpoint()));
    }

    @Override
    public Result<List<CloudServerInstanceRespDTO>> getInstances(String regionId) {
        try {
            List<CloudServerInstanceRespDTO> ecsList = Lists.newArrayList();
            DescribeInstancesRequest request = new DescribeInstancesRequest().setPageSize(100).setRegionId(regionId);
            DescribeInstancesResponse resp = client.describeInstances(request);
            resp.getBody().getInstances().getInstance().forEach(instance -> {
                CloudServerInstanceRespDTO.CloudServerInstanceRespDTOBuilder builder = CloudServerInstanceRespDTO.builder()
                        .instanceId(instance.getInstanceId())
                        .hostname(instance.getInstanceName())
                        .createdTime(DateUtil.format(DateUtil.parse(instance.getCreationTime(), "yyyy-MM-dd'T'HH:mm'Z'"), "yyyy-MM-dd HH:mm:ss"))
                        .operateSystem(instance.getOSName())
                        .status(instance.getStatus())
                        .privateIpAddress(instance.getVpcAttributes().getPrivateIpAddress().getIpAddress().get(0))
                        .expiredTime(DateUtil.format(DateUtil.parse(instance.getExpiredTime(), "yyyy-MM-dd'T'HH:mm'Z'"), "yyyy-MM-dd HH:mm:ss"))
                        .type(ECS);
                if (!"Stopped".equalsIgnoreCase(instance.getStatus())) {
                    builder.publicIpAddress(instance.getPublicIpAddress().getIpAddress().get(0));
                }
                ecsList.add(builder.build());
            });
            return Result.getSuccessful(ecsList);
        } catch (Exception e) {
            log.error("获取ECS实例列表失败", e);
            return Result.getError(400, "获取ECS实例列表失败");
        }
    }

    @Override
    public Result<CloudServerPriceRespDTO> getRenewalPrice(String regionId, String instanceId) {
        try {
            DescribeRenewalPriceRequest request = new DescribeRenewalPriceRequest()
                    .setResourceId(instanceId).setRegionId(regionId);
            String price = client.describeRenewalPrice(request)
                    .getBody()
                    .getPriceInfo().getPrice().getTradePrice().toString();
            return Result.getSuccessful(new CloudServerPriceRespDTO().setPrice(new BigDecimal(price)));
        } catch (Exception e) {
            log.error("获取ECS实例价格失败", e);
            return Result.getError(400, "获取ECS实例价格失败");
        }
    }
}

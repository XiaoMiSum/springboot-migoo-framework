package xyz.migoo.framework.aliyun.core.client.impl;

import cn.hutool.core.date.DateUtil;
import com.aliyun.rds20140815.Client;
import com.aliyun.rds20140815.models.DescribeDBInstancesRequest;
import com.aliyun.rds20140815.models.DescribeDBInstancesResponse;
import com.aliyun.rds20140815.models.DescribeDBInstancesResponseBody;
import com.aliyun.rds20140815.models.DescribeRenewalPriceRequest;
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

import static xyz.migoo.framework.aliyun.core.enums.CloudServerType.RDS;

@Slf4j
public class AliCloudServiceRDSClient extends AbstractCloudServerClient {

    private Client client;

    public AliCloudServiceRDSClient(CloudServiceProperties properties) throws Exception {
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
            List<CloudServerInstanceRespDTO> rdsList = Lists.newArrayList();
            DescribeDBInstancesRequest request = new DescribeDBInstancesRequest().setPageSize(100).setRegionId(regionId);
            DescribeDBInstancesResponse resp = client.describeDBInstances(request);
            for (DescribeDBInstancesResponseBody.DescribeDBInstancesResponseBodyItemsDBInstance instance : resp.getBody().getItems().getDBInstance()) {
                rdsList.add(CloudServerInstanceRespDTO.builder().instanceId(instance.getDBInstanceId())
                        .createdTime(DateUtil.format(DateUtil.parse(instance.getCreateTime(), "yyyy-MM-dd'T'HH:mm:ss'Z'"), "yyyy-MM-dd HH:mm:ss"))
                        .operateSystem(instance.getEngine())
                        .status(instance.getDBInstanceStatus())
                        .ipAddress(instance.getConnectionString())
                        .expiredTime(DateUtil.format(DateUtil.parse(instance.getExpireTime(), "yyyy-MM-dd'T'HH:mm:ss'Z'"), "yyyy-MM-dd HH:mm:ss"))
                        .type(RDS).build());
            }
            return Result.getSuccessful(rdsList);
        } catch (Exception e) {
            log.error("获取RDS实例列表失败", e);
            return Result.getError(400, "获取RDS实例列表失败");
        }

    }

    @Override
    public Result<CloudServerPriceRespDTO> getRenewalPrice(String regionId, String instanceId) {
        try {
            DescribeRenewalPriceRequest request = new DescribeRenewalPriceRequest()
                    .setDBInstanceId(instanceId).setRegionId(regionId);
            String price = client.describeRenewalPrice(request)
                    .getBody()
                    .getPriceInfo().getTradePrice().toString();
            return Result.getSuccessful(new CloudServerPriceRespDTO().setPrice(new BigDecimal(price)));
        } catch (Exception e) {
            log.error("获取RDS实例价格失败", e);
            return Result.getError(400, "获取RDS实例价格失败");
        }
    }
}

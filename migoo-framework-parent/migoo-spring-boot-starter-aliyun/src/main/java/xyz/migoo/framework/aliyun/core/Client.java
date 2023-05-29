package xyz.migoo.framework.aliyun.core;

import com.aliyun.ecs20140526.models.DescribeInstancesRequest;
import com.aliyun.ecs20140526.models.DescribeInstancesResponse;
import com.aliyun.ecs20140526.models.DescribeInstancesResponseBody;
import com.aliyun.ecs20140526.models.DescribeRenewalPriceRequest;
import com.aliyun.rds20140815.models.DescribeDBInstancesRequest;
import com.aliyun.rds20140815.models.DescribeDBInstancesResponse;
import com.aliyun.rds20140815.models.DescribeDBInstancesResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.common.util.date.DateUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Client {

    private static final Client client = new Client();

    private final Map<String, com.aliyun.ecs20140526.Client> ECS_CLIENTS = Maps.newHashMap();
    private final Map<String, com.aliyun.rds20140815.Client> RDS_CLIENTS = Maps.newHashMap();

    public static List<CloudServer> getServers(String keyId, String secret, String regionId, String host) {
        List<CloudServer> servers = Lists.newArrayList();
        servers.addAll(client.getEcsList(keyId, secret, regionId, host));
        servers.addAll(client.getRdsList(keyId, secret, regionId, host));
        return servers;
    }

    private BigDecimal getRenewalPrice(com.aliyun.ecs20140526.Client client, String regionId, String instanceId) {
        try {
            String price = client.describeRenewalPrice(new DescribeRenewalPriceRequest().setResourceId(instanceId).setRegionId(regionId))
                    .getBody()
                    .getPriceInfo().getPrice().getTradePrice().toString();
            return new BigDecimal(price);
        } catch (Exception e) {
            log.error("获取ECS实例价格失败", e);
            return new BigDecimal("-1");
        }
    }

    private BigDecimal getRenewalPrice(com.aliyun.rds20140815.Client client, String regionId, String instanceId) {
        try {
            String price = client.describeRenewalPrice(new com.aliyun.rds20140815.models.DescribeRenewalPriceRequest()
                            .setDBInstanceId(instanceId).setRegionId(regionId))
                    .getBody().getPriceInfo().getTradePrice().toString();
            return new BigDecimal(price);
        } catch (Exception e) {
            log.error("获取RDS实例价格失败", e);
            return new BigDecimal("-1");
        }
    }

    private synchronized com.aliyun.ecs20140526.Client getEcsClient(String keyId, String secret, String host) throws Exception {
        com.aliyun.ecs20140526.Client client = ECS_CLIENTS.get(keyId);
        if (Objects.isNull(client)) {
            client = new com.aliyun.ecs20140526.Client(getConfig(keyId, secret, host));
            ECS_CLIENTS.put(keyId, client);
        }
        return client;
    }

    private synchronized com.aliyun.rds20140815.Client getRdsClient(String keyId, String secret, String host) throws Exception {
        com.aliyun.rds20140815.Client client = RDS_CLIENTS.get(keyId);
        if (Objects.isNull(client)) {
            client = new com.aliyun.rds20140815.Client(getConfig(keyId, secret, host));
            RDS_CLIENTS.put(keyId, client);
        }
        return client;
    }

    private Config getConfig(String keyId, String secret, String host) {
        return new Config()
                .setAccessKeyId(keyId)
                .setAccessKeySecret(secret)
                .setEndpoint(host);
    }

    private List<CloudServer> getEcsList(String keyId, String secret, String host, String regionId) throws ServiceException {
        List<CloudServer> ecsList = Lists.newArrayList();
        try {
            DescribeInstancesRequest request = new DescribeInstancesRequest().setPageSize(100).setRegionId(regionId);
            com.aliyun.ecs20140526.Client client = getEcsClient(keyId, secret, host);
            DescribeInstancesResponse resp = getEcsClient(keyId, secret, host).describeInstances(request);
            for (DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance instance : resp.getBody().getInstances().getInstance()) {
                ecsList.add(CloudServer.builder().instanceId(instance.getInstanceId())
                        .createTime(DateUtils.format(instance.getCreationTime(), "yyyy-MM-dd HH:mm:ss"))
                        .operateSystem(instance.getOSName())
                        .status(instance.getStatus())
                        .ipAddress(instance.getPublicIpAddress().getIpAddress().get(0))
                        .expiredTime(DateUtils.format(instance.getExpiredTime(), "yyyy-MM-dd HH:mm:ss"))
                        .price(getRenewalPrice(client, regionId, instance.getInstanceId()))
                        .type(ServerType.RDS).build());
            }
        } catch (Exception e) {
            log.error("获取ECS实例列表失败", e);
        }
        return ecsList;
    }

    private List<CloudServer> getRdsList(String keyId, String secret, String host, String regionId) throws ServiceException {
        List<CloudServer> rdsList = Lists.newArrayList();
        try {
            DescribeDBInstancesRequest request = new DescribeDBInstancesRequest().setPageSize(100).setRegionId(regionId);
            com.aliyun.rds20140815.Client client = getRdsClient(keyId, secret, host);
            DescribeDBInstancesResponse resp = client.describeDBInstances(request);
            for (DescribeDBInstancesResponseBody.DescribeDBInstancesResponseBodyItemsDBInstance instance : resp.getBody().getItems().getDBInstance()) {
                rdsList.add(CloudServer.builder().instanceId(instance.getDBInstanceId())
                        .createTime(DateUtils.format(instance.getCreateTime(), "yyyy-MM-dd HH:mm:ss"))
                        .operateSystem(instance.getEngine())
                        .status(instance.getDBInstanceStatus())
                        .ipAddress(instance.getConnectionString())
                        .expiredTime(DateUtils.format(instance.getExpireTime(), "yyyy-MM-dd HH:mm:ss"))
                        .price(getRenewalPrice(client, regionId, instance.getDBInstanceId()))
                        .type(ServerType.RDS).build());
            }
        } catch (Exception e) {
            log.error("获取RDS实例列表失败", e);
            throw new RuntimeException("获取ECS实例列表失败");
        }
        return rdsList;
    }
}

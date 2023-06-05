package xyz.migoo.framework.aliyun.core.client.impl;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;
import xyz.migoo.framework.aliyun.core.client.CloudServiceClient;
import xyz.migoo.framework.aliyun.core.client.CloudServiceClientFactory;
import xyz.migoo.framework.aliyun.core.enums.CloudServerType;
import xyz.migoo.framework.aliyun.core.enums.CloudServiceProvide;
import xyz.migoo.framework.aliyun.core.property.CloudServiceProperties;

import java.util.Map;

import static xyz.migoo.framework.aliyun.core.enums.CloudServerType.ECS;
import static xyz.migoo.framework.aliyun.core.enums.CloudServerType.RDS;
import static xyz.migoo.framework.aliyun.core.enums.CloudServiceProvide.ALI_CLOUD;

@Component
public class CloudServiceClientFactoryImpl implements CloudServiceClientFactory {

    private final Map<String, CloudServiceClient> clientMap = Maps.newHashMap();

    @Override
    public CloudServiceClient getClient(String accessKeyId, CloudServerType serverType) {
        return clientMap.get(accessKeyId + serverType);
    }

    @Override
    public void createClient(CloudServiceProperties properties, CloudServiceProvide provide) throws Exception {
        CloudServiceClient client = null;
        if (provide == ALI_CLOUD && properties.getServerType() == ECS) {
            client = new AliCloudServiceECSClient(properties);
        } else if (provide == ALI_CLOUD && properties.getServerType() == RDS) {
            client = new AliCloudServiceRDSClient(properties);
        }
        clientMap.put(properties.getAccessKeyId() + properties.getServerType(), client);
    }
}

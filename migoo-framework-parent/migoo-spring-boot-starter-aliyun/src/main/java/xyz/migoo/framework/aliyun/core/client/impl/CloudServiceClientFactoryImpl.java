package xyz.migoo.framework.aliyun.core.client.impl;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;
import xyz.migoo.framework.aliyun.core.client.CloudServiceClient;
import xyz.migoo.framework.aliyun.core.client.CloudServiceClientFactory;
import xyz.migoo.framework.aliyun.core.enums.CloudServerType;
import xyz.migoo.framework.aliyun.core.enums.CloudServiceProvide;
import xyz.migoo.framework.aliyun.core.property.CloudServiceProperties;

import java.util.Map;

@Component
public class CloudServiceClientFactoryImpl implements CloudServiceClientFactory {

    private final Map<String, CloudServiceClient> clientMap = Maps.newHashMap();

    @Override
    public CloudServiceClient getClient(String accessKeyId, CloudServerType serverType) {
        return clientMap.get(accessKeyId + serverType);
    }

    @Override
    public void createClient(CloudServiceProperties properties, CloudServiceProvide provide) throws Exception {
        if (clientMap.containsKey(properties.getAccessKeyId() + properties.getServerType())) {
            return;
        }
        CloudServiceClient client = null;
        if (provide == CloudServiceProvide.AliCloud && properties.getServerType() == CloudServerType.ESC) {
            client = new AliCloudServiceECSClient(properties);
        } else if (provide == CloudServiceProvide.AliCloud && properties.getServerType() == CloudServerType.RDS) {
            client = new AliCloudServiceRDSClient(properties);
        }
        clientMap.put(properties.getAccessKeyId() + properties.getServerType(), client);
    }
}

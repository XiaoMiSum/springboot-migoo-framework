package xyz.migoo.framework.aliyun.core.client;


import xyz.migoo.framework.aliyun.core.enums.CloudServerType;
import xyz.migoo.framework.aliyun.core.enums.CloudServiceProvide;
import xyz.migoo.framework.aliyun.core.property.CloudServiceProperties;

/**
 * 云服务客户端的工厂接口
 *
 * @author xiaomi
 */
public interface CloudServiceClientFactory {

    /**
     * 获得云服务 Client
     *
     * @param accessKeyId 访问key
     * @param serverType  服务器类型
     * @return 云服务 Client
     */
    CloudServiceClient getClient(String accessKeyId, CloudServerType serverType);

    /**
     * 创建云服务 Client
     *
     * @param properties 配置对象
     * @param provide    云服务提供商
     */
    void createClient(CloudServiceProperties properties, CloudServiceProvide provide) throws Exception;

}

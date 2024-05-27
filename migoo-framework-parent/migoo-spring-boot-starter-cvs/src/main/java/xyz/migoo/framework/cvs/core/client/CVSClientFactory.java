package xyz.migoo.framework.cvs.core.client;

import xyz.migoo.framework.cvs.core.enums.CVSMachineType;
import xyz.migoo.framework.cvs.core.property.CVSClientProperties;

public interface CVSClientFactory {

    /**
     * 获得短信 Client
     *
     * @param channelId 渠道编号
     * @param type      云服务器类型
     * @return 云服务 Client
     */
    CVSClient getClient(Long channelId, CVSMachineType type);


    /**
     * 创建短信 Client
     *
     * @param properties 配置对象
     * @param type       云服务器类型
     */
    void createOrUpdateClient(CVSClientProperties properties, CVSMachineType type);
}

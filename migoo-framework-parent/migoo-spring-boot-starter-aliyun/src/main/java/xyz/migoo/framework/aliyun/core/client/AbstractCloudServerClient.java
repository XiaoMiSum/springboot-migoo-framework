package xyz.migoo.framework.aliyun.core.client;

import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.aliyun.core.property.CloudServiceProperties;

@Slf4j
public abstract class AbstractCloudServerClient implements CloudServiceClient {

    protected volatile CloudServiceProperties properties;

    public AbstractCloudServerClient(CloudServiceProperties properties) throws Exception {
        this.properties = prepareProperties(properties);
        this.init();
    }


    protected CloudServiceProperties prepareProperties(CloudServiceProperties properties) {
        return properties;
    }

    protected abstract void init() throws Exception;

}

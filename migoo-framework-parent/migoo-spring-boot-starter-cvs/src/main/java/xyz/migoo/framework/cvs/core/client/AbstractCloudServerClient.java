package xyz.migoo.framework.cvs.core.client;

import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.cvs.core.property.CloudServiceProperties;

@Slf4j
public abstract class AbstractCloudServerClient implements CloudServiceClient {

    public AbstractCloudServerClient(CloudServiceProperties properties) {
        initialization(properties);
    }

    protected abstract void initialization(CloudServiceProperties properties);

}

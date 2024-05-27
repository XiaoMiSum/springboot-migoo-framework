package xyz.migoo.framework.cvs.core.client;

import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.cvs.core.property.CVSClientProperties;

@Slf4j
public abstract class AbstractCVSClient implements CVSClient {

    /**
     * 短信渠道配置
     */
    protected volatile CVSClientProperties properties;

    public AbstractCVSClient(CVSClientProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化
     */
    public final void initialization() {
        doInitialization();
        log.info("[init][配置({}) 初始化完成]", properties);
    }

    /**
     * 自定义初始化
     */
    protected abstract void doInitialization();

    public final void refresh(CVSClientProperties properties) {
        // 判断是否更新
        if (properties.equals(this.properties)) {
            return;
        }
        log.info("[refresh][配置({})发生变化，重新初始化]", properties);
        this.properties = properties;
        // 初始化
        this.initialization();
    }

}

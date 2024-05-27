package xyz.migoo.framework.cvs.core.client;

import cn.hutool.core.util.StrUtil;
import org.springframework.util.Assert;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.cvs.core.client.aliyun.ECSClient;
import xyz.migoo.framework.cvs.core.client.aliyun.RDSClient;
import xyz.migoo.framework.cvs.core.enums.CVSMachineType;
import xyz.migoo.framework.cvs.core.enums.CVSProvide;
import xyz.migoo.framework.cvs.core.property.CVSClientProperties;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static xyz.migoo.framework.cvs.core.enums.CVSMachineType.ECS;
import static xyz.migoo.framework.cvs.core.enums.CVSMachineType.RDS;
import static xyz.migoo.framework.cvs.core.enums.CVSProvide.ALI_CLOUD;
import static xyz.migoo.framework.cvs.core.enums.CVSProvide.TENCENT;

public class CVSClientFactoryImpl implements CVSClientFactory {


    /**
     * 短信客户端 Map
     * key：渠道编号，使用 {@link CVSClientProperties#getId()} + _CVSMachineType
     */
    private final ConcurrentMap<String, AbstractCVSClient> channelIdClients = new ConcurrentHashMap<>();

    @Override
    public CVSClient getClient(Long id, CVSMachineType type) {
        return channelIdClients.get(id + "_" + type);
    }

    @Override
    public void createOrUpdateClient(CVSClientProperties properties, CVSMachineType type) {
        String key = properties.getId() + "_" + type;
        AbstractCVSClient client = channelIdClients.get(key);
        if (client == null) {
            client = this.createClient(properties, type);
            client.initialization();
            channelIdClients.put(key, client);
        } else {
            client.refresh(properties);
        }
    }

    private AbstractCVSClient createClient(CVSClientProperties properties, CVSMachineType type) {
        CVSProvide provide = CVSProvide.valueOf(properties.getCode().toUpperCase(Locale.ROOT));
        Assert.notNull(provide, String.format("服务商(%s) 为空", properties.getCode()));
        // 创建客户端
        if (provide == ALI_CLOUD && type == ECS) {
            return new ECSClient(properties);
        } else if (provide == ALI_CLOUD && type == RDS) {
            return new RDSClient(properties);
        } else if (provide == TENCENT && type == ECS) {
            return new xyz.migoo.framework.cvs.core.client.tencent.ECSClient(properties);
        } else if (provide == TENCENT && type == RDS) {
            return new xyz.migoo.framework.cvs.core.client.tencent.RDSClient(properties);
        }
        throw ServiceExceptionUtil.get(new ErrorCode(-1,
                StrUtil.format("无可用云服务商客户端: {}, {}", provide.name(), type.name())));
    }
}

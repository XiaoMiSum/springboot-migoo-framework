package xyz.migoo.framework.infra.service.cvs;

import cn.hutool.core.util.StrUtil;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.object.BeanUtils;
import xyz.migoo.framework.common.util.thread.VirtualThreadUtils;
import xyz.migoo.framework.cvs.core.client.CVSClient;
import xyz.migoo.framework.cvs.core.client.CVSClientFactory;
import xyz.migoo.framework.cvs.core.client.dto.CVMachineInstanceRespDTO;
import xyz.migoo.framework.cvs.core.client.dto.Option;
import xyz.migoo.framework.cvs.core.enums.CVSMachineType;
import xyz.migoo.framework.cvs.core.property.CVSClientProperties;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSMachinePageQueryReqVO;
import xyz.migoo.framework.infra.convert.cvs.CVSMachineConvert;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSMachineDO;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSProviderDO;
import xyz.migoo.framework.infra.dal.mapper.cvs.CVSMachineMapper;
import xyz.migoo.framework.infra.dal.mapper.cvs.CVSProviderMapper;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import static xyz.migoo.framework.common.util.cache.CacheUtils.buildAsyncReloadingCache;
import static xyz.migoo.framework.cvs.core.enums.CVSMachineType.ECS;
import static xyz.migoo.framework.cvs.core.enums.CVSMachineType.RDS;


@Service
@Slf4j
public class CVSMachineServiceImpl implements CVSMachineService {

    @Resource
    private CVSProviderMapper providerMapper;
    @Resource
    private CVSMachineMapper machineMapper;
    @Resource
    private CVSClientFactory clientFactory;


    /**
     * {@link CVSClient} 缓存，通过它异步刷新 smsClientFactory
     */
    @Getter
    private final LoadingCache<String, CVSClient> clientCache = buildAsyncReloadingCache(Duration.ofSeconds(10L),
            new CacheLoader<>() {

                @Override
                public CVSClient load(String key) {
                    // 查询，然后尝试刷新
                    String[] keys = StrUtil.splitToArray(key, "_");
                    Long id = Long.parseLong(keys[0]);
                    CVSMachineType type = CVSMachineType.valueOf(keys[1].toUpperCase(Locale.ROOT));
                    CVSProviderDO channel = providerMapper.selectById(id);
                    if (channel != null) {
                        CVSClientProperties properties = BeanUtils.toBean(channel, CVSClientProperties.class);
                        clientFactory.createOrUpdateClient(properties, type);
                    }
                    return clientFactory.getClient(id, type);
                }
            });


    @Override
    public PageResult<CVSMachineDO> getPage(CVSMachinePageQueryReqVO req) {
        return machineMapper.selectPage(req);
    }

    @Override
    public List<CVSMachineDO> getList() {
        return machineMapper.selectList();
    }

    @Override
    public void sync() {
        VirtualThreadUtils.submit(() -> {
            providerMapper.selectList().forEach(account -> {
                sync(account, ECS);
                sync(account, RDS);
            });
        });
    }

    private void sync(CVSProviderDO provider, CVSMachineType type) {
        CVSClient client = clientCache.getUnchecked(provider.getId() + "_" + type);
        Result<List<CVMachineInstanceRespDTO>> result = client.getInstances(provider.getRegion());
        if (result.isSuccessful()) {
            result.getData().forEach(item -> {
                CVSMachineDO server = CVSMachineConvert.INSTANCE.convert(item, provider.getAccount());
                machineMapper.save(server);
            });
        }
    }

    @Override
    public void update(CVSMachineDO bean) {
        machineMapper.updateById(bean);
    }

    @Override
    public void remove(Long id) {
        machineMapper.deleteById(id);
    }

    @Override
    public Result<?> option(Long id, Option option) {
        CVSMachineDO cvs = machineMapper.selectById(id);
        CVSProviderDO provider = providerMapper.selectOne(cvs.getAccount());
        CVSClient client = clientCache.getUnchecked(provider.getId() + "_" + cvs.getMachineType());
        return switch (option) {
            case stop -> client.stop(cvs.getInstanceId());
            case start -> client.start(cvs.getInstanceId());
            case restart -> client.reboot(cvs.getInstanceId());
        };
    }

}

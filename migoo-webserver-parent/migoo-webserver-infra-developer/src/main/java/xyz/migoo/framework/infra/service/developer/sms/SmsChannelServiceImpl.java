package xyz.migoo.framework.infra.service.developer.sms;

import cn.hutool.core.util.StrUtil;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.util.object.BeanUtils;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelCreateReqVO;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelPageReqVO;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelUpdateReqVO;
import xyz.migoo.framework.infra.convert.developer.sms.SmsChannelConvert;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsChannelDO;
import xyz.migoo.framework.infra.dal.mapper.developer.sms.SmsChannelMapper;
import xyz.migoo.framework.infra.dal.mapper.developer.sms.SmsTemplateMapper;
import xyz.migoo.framework.sms.core.client.SmsClient;
import xyz.migoo.framework.sms.core.client.SmsClientFactory;
import xyz.migoo.framework.sms.core.property.SmsChannelProperties;

import java.time.Duration;
import java.util.List;

import static xyz.migoo.framework.common.util.cache.CacheUtils.buildAsyncReloadingCache;
import static xyz.migoo.framework.infra.enums.ErrorCodeConstants.SMS_CHANNEL_HAS_CHILDREN;
import static xyz.migoo.framework.infra.enums.ErrorCodeConstants.SMS_CHANNEL_NOT_EXISTS;


@Service
@Slf4j
public class SmsChannelServiceImpl implements SmsChannelService {

    @Resource
    private SmsClientFactory smsClientFactory;

    @Resource
    private SmsChannelMapper smsChannelMapper;
    /**
     * {@link SmsClient} 缓存，通过它异步刷新 smsClientFactory
     */
    @Getter
    private final LoadingCache<Long, SmsClient> idClientCache = buildAsyncReloadingCache(Duration.ofSeconds(10L),
            new CacheLoader<>() {

                @Override
                public SmsClient load(Long id) {
                    // 查询，然后尝试刷新
                    SmsChannelDO channel = smsChannelMapper.selectById(id);
                    if (channel != null) {
                        SmsChannelProperties properties = BeanUtils.toBean(channel, SmsChannelProperties.class);
                        smsClientFactory.createOrUpdateSmsClient(properties);
                    }
                    return smsClientFactory.getSmsClient(id);
                }
            });
    /**
     * {@link SmsClient} 缓存，通过它异步刷新 smsClientFactory
     */
    @Getter
    private final LoadingCache<String, SmsClient> codeClientCache = buildAsyncReloadingCache(Duration.ofSeconds(60L),
            new CacheLoader<>() {
                @Override
                public SmsClient load(String code) {
                    // 查询，然后尝试刷新
                    SmsChannelDO channel = smsChannelMapper.selectByCode(code);
                    if (channel != null) {
                        SmsChannelProperties properties = BeanUtils.toBean(channel, SmsChannelProperties.class);
                        smsClientFactory.createOrUpdateSmsClient(properties);
                    }
                    return smsClientFactory.getSmsClient(code);
                }
            });
    @Resource
    private SmsTemplateMapper smsTemplateMapper;

    @Override
    public Long createSmsChannel(SmsChannelCreateReqVO createReqVO) {
        // 插入
        SmsChannelDO smsChannel = SmsChannelConvert.INSTANCE.convert(createReqVO);
        smsChannelMapper.insert(smsChannel);
        return smsChannel.getId();
    }

    @Override
    public void updateSmsChannel(SmsChannelUpdateReqVO updateReqVO) {
        // 校验存在
        validateSmsChannelExists(updateReqVO.getId());
        // 更新
        SmsChannelDO updateObj = SmsChannelConvert.INSTANCE.convert(updateReqVO);
        smsChannelMapper.updateById(updateObj);
        // 清空缓存
        clearCache(updateReqVO.getId(), updateObj.getCode());
    }

    @Override
    public void deleteSmsChannel(Long id) {
        // 校验存在
        SmsChannelDO channel = validateSmsChannelExists(id);
        // 校验是否有在使用该账号的模版
        if (smsTemplateMapper.selectCountByChannelId(id) > 0) {
            throw ServiceExceptionUtil.get(SMS_CHANNEL_HAS_CHILDREN);
        }
        // 删除
        smsChannelMapper.deleteById(id);
        clearCache(channel.getId(), channel.getCode());
    }

    private SmsChannelDO validateSmsChannelExists(Long id) {
        SmsChannelDO channel = smsChannelMapper.selectById(id);
        if (channel == null) {
            throw ServiceExceptionUtil.get(SMS_CHANNEL_NOT_EXISTS);
        }
        return channel;
    }

    @Override
    public SmsChannelDO getSmsChannel(Long id) {
        return smsChannelMapper.selectById(id);
    }

    @Override
    public List<SmsChannelDO> getSmsChannelList() {
        return smsChannelMapper.selectList();
    }

    @Override
    public PageResult<SmsChannelDO> getSmsChannelPage(SmsChannelPageReqVO pageReqVO) {
        return smsChannelMapper.selectPage(pageReqVO);
    }


    @Override
    public SmsClient getSmsClient(Long id) {
        return idClientCache.getUnchecked(id);
    }

    @Override
    public SmsClient getSmsClient(String code) {
        return codeClientCache.getUnchecked(code);
    }

    /**
     * 清空指定渠道编号的缓存
     *
     * @param id   渠道编号
     * @param code 渠道编码
     */
    private void clearCache(Long id, String code) {
        idClientCache.invalidate(id);
        if (StrUtil.isNotEmpty(code)) {
            codeClientCache.invalidate(code);
        }
    }

}

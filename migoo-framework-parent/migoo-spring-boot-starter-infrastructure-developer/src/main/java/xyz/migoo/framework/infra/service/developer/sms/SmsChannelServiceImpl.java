package xyz.migoo.franework.infra.service.developer.sms;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.sms.core.client.SmsClientFactory;
import xyz.migoo.framework.sms.core.property.SmsChannelProperties;
import xyz.migoo.franework.infra.controller.developer.sms.vo.channel.SmsChannelCreateReqVO;
import xyz.migoo.franework.infra.controller.developer.sms.vo.channel.SmsChannelPageReqVO;
import xyz.migoo.franework.infra.controller.developer.sms.vo.channel.SmsChannelUpdateReqVO;
import xyz.migoo.franework.infra.convert.developer.sms.SmsChannelConvert;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsChannelDO;
import xyz.migoo.franework.infra.dal.mapper.developer.sms.SmsChannelMapper;
import xyz.migoo.franework.infra.dal.mapper.developer.sms.SmsTemplateMapper;

import java.util.List;

import static xyz.migoo.franework.infra.enums.ErrorCodeConstants.SMS_CHANNEL_HAS_CHILDREN;
import static xyz.migoo.franework.infra.enums.ErrorCodeConstants.SMS_CHANNEL_NOT_EXISTS;


@Service
@Slf4j
public class SmsChannelServiceImpl implements SmsChannelService {

    @Resource
    private SmsClientFactory smsClientFactory;

    @Resource
    private SmsChannelMapper smsChannelMapper;

    @Resource
    private SmsTemplateMapper smsTemplateMapper;

    @Override
    public void initLocalCache() {
        // 第一步：查询数据
        List<SmsChannelDO> channels = smsChannelMapper.selectList();
        log.info("[initLocalCache][缓存短信渠道，数量为:{}]", channels.size());

        // 第二步：构建缓存：创建或更新短信 Client
        List<SmsChannelProperties> propertiesList = SmsChannelConvert.INSTANCE.convertList02(channels);
        propertiesList.forEach(properties -> smsClientFactory.createOrUpdateSmsClient(properties));
    }

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
    }

    @Override
    public void deleteSmsChannel(Long id) {
        // 校验存在
        validateSmsChannelExists(id);
        // 校验是否有在使用该账号的模版
        if (smsTemplateMapper.selectCountByChannelId(id) > 0) {
            throw ServiceExceptionUtil.get(SMS_CHANNEL_HAS_CHILDREN);
        }
        // 删除
        smsChannelMapper.deleteById(id);
    }

    private void validateSmsChannelExists(Long id) {
        if (smsChannelMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.get(SMS_CHANNEL_NOT_EXISTS);
        }
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

}

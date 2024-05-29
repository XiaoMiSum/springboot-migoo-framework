package xyz.migoo.framework.infra.service.developer.sms;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.enums.CommonStatusEnum;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.util.collection.CollectionUtils;
import xyz.migoo.framework.sms.core.client.SmsClientFactory;
import xyz.migoo.framework.infra.controller.developer.sms.vo.template.SmsTemplateCreateReqVO;
import xyz.migoo.framework.infra.controller.developer.sms.vo.template.SmsTemplatePageReqVO;
import xyz.migoo.framework.infra.controller.developer.sms.vo.template.SmsTemplateUpdateReqVO;
import xyz.migoo.framework.infra.convert.developer.sms.SmsTemplateConvert;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsChannelDO;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsTemplateDO;
import xyz.migoo.framework.infra.dal.mapper.developer.sms.SmsTemplateMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static xyz.migoo.framework.infra.enums.DeveloperErrorCodeConstants.*;

@Service
@Slf4j
public class SmsTemplateServiceImpl implements SmsTemplateService {

    /**
     * 正则表达式，匹配 {} 中的变量
     */
    private static final Pattern PATTERN_PARAMS = Pattern.compile("\\{(.*?)}");

    @Resource
    private SmsTemplateMapper smsTemplateMapper;

    @Resource
    private SmsChannelService smsChannelService;

    @Resource
    private SmsClientFactory smsClientFactory;

    /**
     * 短信模板缓存
     * key：短信模板编码 {@link SmsTemplateDO#getCode()}
     * <p>
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter // 为了方便测试，这里提供 getter 方法
    private volatile Map<String, SmsTemplateDO> smsTemplateCache;

    @Override
    @PostConstruct
    public void initLocalCache() {
        // 第一步：查询数据
        List<SmsTemplateDO> smsTemplateList = smsTemplateMapper.selectList();
        log.info("[initLocalCache][缓存短信模版，数量为:{}]", smsTemplateList.size());

        // 第二步：构建缓存
        smsTemplateCache = CollectionUtils.convertMap(smsTemplateList, SmsTemplateDO::getCode);
    }

    @Override
    public SmsTemplateDO getSmsTemplateByCodeFromCache(String code) {
        return smsTemplateCache.get(code);
    }

    @Override
    public String formatSmsTemplateContent(String content, Map<String, Object> params) {
        return StrUtil.format(content, params);
    }

    @Override
    public SmsTemplateDO getSmsTemplateByCode(String code) {
        return smsTemplateMapper.selectByCode(code);
    }

    @VisibleForTesting
    public List<String> parseTemplateContentParams(String content) {
        return ReUtil.findAllGroup1(PATTERN_PARAMS, content);
    }

    @Override
    public Long createSmsTemplate(SmsTemplateCreateReqVO createReqVO) {
        // 校验短信渠道
        SmsChannelDO channelDO = validateSmsChannel(createReqVO.getChannelId());
        // 校验短信编码是否重复
        validateSmsTemplateCodeDuplicate(null, createReqVO.getCode());

        // 插入
        SmsTemplateDO template = SmsTemplateConvert.INSTANCE.convert(createReqVO);
        template.setParams(parseTemplateContentParams(template.getContent()));
        template.setChannelCode(channelDO.getCode());
        smsTemplateMapper.insert(template);
        // 返回
        return template.getId();
    }

    @Override
    public void updateSmsTemplate(SmsTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        validateSmsTemplateExists(updateReqVO.getId());
        // 校验短信渠道
        SmsChannelDO channelDO = validateSmsChannel(updateReqVO.getChannelId());
        // 校验短信编码是否重复
        validateSmsTemplateCodeDuplicate(updateReqVO.getId(), updateReqVO.getCode());
        // 更新
        SmsTemplateDO updateObj = SmsTemplateConvert.INSTANCE.convert(updateReqVO);
        updateObj.setParams(parseTemplateContentParams(updateObj.getContent()));
        updateObj.setChannelCode(channelDO.getCode());
        smsTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteSmsTemplate(Long id) {
        // 校验存在
        validateSmsTemplateExists(id);
        // 更新
        smsTemplateMapper.deleteById(id);
    }

    private void validateSmsTemplateExists(Long id) {
        if (smsTemplateMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.get(SMS_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public SmsTemplateDO getSmsTemplate(Long id) {
        return smsTemplateMapper.selectById(id);
    }

    @Override
    public List<SmsTemplateDO> getSmsTemplateList(Collection<Long> ids) {
        return smsTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SmsTemplateDO> getSmsTemplatePage(SmsTemplatePageReqVO pageReqVO) {
        return smsTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public Long countByChannelId(Long channelId) {
        return smsTemplateMapper.selectCountByChannelId(channelId);
    }

    @Override
    public List<SmsTemplateDO> getSmsTemplates() {
        return smsTemplateMapper.selectList();
    }

    @VisibleForTesting
    public SmsChannelDO validateSmsChannel(Long channelId) {
        SmsChannelDO channelDO = smsChannelService.getSmsChannel(channelId);
        if (channelDO == null) {
            throw ServiceExceptionUtil.get(SMS_CHANNEL_NOT_EXISTS);
        }
        if (!Objects.equals(channelDO.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw ServiceExceptionUtil.get(SMS_CHANNEL_DISABLE);
        }
        return channelDO;
    }

    @VisibleForTesting
    public void validateSmsTemplateCodeDuplicate(Long id, String code) {
        SmsTemplateDO template = smsTemplateMapper.selectByCode(code);
        if (template == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null) {
            throw ServiceExceptionUtil.get(SMS_TEMPLATE_CODE_DUPLICATE, code);
        }
        if (!template.getId().equals(id)) {
            throw ServiceExceptionUtil.get(SMS_TEMPLATE_CODE_DUPLICATE, code);
        }
    }

}

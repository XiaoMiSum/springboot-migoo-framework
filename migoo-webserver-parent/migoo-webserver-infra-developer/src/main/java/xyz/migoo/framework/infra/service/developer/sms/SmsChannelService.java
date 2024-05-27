package xyz.migoo.framework.infra.service.developer.sms;

import jakarta.validation.Valid;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelCreateReqVO;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelPageReqVO;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelUpdateReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsChannelDO;
import xyz.migoo.framework.sms.core.client.SmsClient;

import java.util.List;

public interface SmsChannelService {


    /**
     * 创建短信渠道
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSmsChannel(@Valid SmsChannelCreateReqVO createReqVO);

    /**
     * 更新短信渠道
     *
     * @param updateReqVO 更新信息
     */
    void updateSmsChannel(@Valid SmsChannelUpdateReqVO updateReqVO);

    /**
     * 删除短信渠道
     *
     * @param id 编号
     */
    void deleteSmsChannel(Long id);

    /**
     * 获得短信渠道
     *
     * @param id 编号
     * @return 短信渠道
     */
    SmsChannelDO getSmsChannel(Long id);

    /**
     * 获得所有短信渠道列表
     *
     * @return 短信渠道列表
     */
    List<SmsChannelDO> getSmsChannelList();

    /**
     * 获得短信渠道分页
     *
     * @param pageReqVO 分页查询
     * @return 短信渠道分页
     */
    PageResult<SmsChannelDO> getSmsChannelPage(SmsChannelPageReqVO pageReqVO);

    /**
     * 获得短信客户端
     *
     * @param id 编号
     * @return 短信客户端
     */
    SmsClient getSmsClient(Long id);
    
    /**
     * 获得短信客户端
     *
     * @param code 编码
     * @return 短信客户端
     */
    SmsClient getSmsClient(String code);
}

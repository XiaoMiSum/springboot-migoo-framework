package xyz.migoo.framework.sms.core.client.dto;

import lombok.Data;
import xyz.migoo.framework.sms.core.enums.SmsTemplateAuditStatusEnum;

/**
 * 短信模板 Response DTO
 *
 * @author xiaomi
 */
@Data
public class SmsTemplateRespDTO {

    /**
     * 模板编号
     */
    private String id;
    /**
     * 短信内容
     */
    private String content;
    /**
     * 审核状态
     * <p>
     * 枚举 {@link SmsTemplateAuditStatusEnum}
     */
    private Integer auditStatus;
    /**
     * 审核未通过的理由
     */
    private String auditReason;

}

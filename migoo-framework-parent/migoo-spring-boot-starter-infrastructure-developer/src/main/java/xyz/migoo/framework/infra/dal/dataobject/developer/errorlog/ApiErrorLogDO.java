package xyz.migoo.framework.infra.dal.dataobject.developer.errorlog;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;

import java.util.Date;

@TableName("infra_error_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorLogDO extends IdEnhanceDO {

    private String applicationName;

    private String requestMethod;

    private String requestUrl;

    private String requestParams;

    private String userIp;

    private Date exceptionTime;

    private String exceptionName;

    private String exceptionClassName;

    private String exceptionFileName;

    private String exceptionMethodName;

    private Integer exceptionLineNumber;

    private String exceptionStackTrace;

    private String exceptionRootCauseMessage;

    private String exceptionMessage;

    private Integer status;
}

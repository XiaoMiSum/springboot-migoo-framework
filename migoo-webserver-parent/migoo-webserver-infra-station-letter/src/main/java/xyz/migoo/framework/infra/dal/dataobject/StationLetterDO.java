package xyz.migoo.framework.infra.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

@TableName(value = "infra_station_letter", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class StationLetterDO extends BaseDO<Long, StationLetterDO> {

    /**
     * 消息编码
     */
    private String code;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 接收用户id
     */
    private Long toUserId;

    /**
     * 接收用户类型
     */
    private String toUserType;

    /**
     * 来源用户id
     */
    private Long fromUserId;

    /**
     * 来源用户类型
     */
    private String fromUserType;

    /**
     * 1 未读、0 已读
     */
    private Integer unread;
}

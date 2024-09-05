package xyz.migoo.framework.infra.service;

import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.stationletter.vo.StationLetterPageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.StationLetterDO;

import java.util.Collection;

public interface StationLetterService {

    PageResult<StationLetterDO> getPage(StationLetterPageReqVO req);

    StationLetterDO get(Long id);

    Long add(String code, String title, String content, String fromUserType, Long toUserId, String toUserType);

    void read(Collection<Long> ids);

    void unread(Collection<Long> ids);

    void remove(Collection<Long> ids);
}

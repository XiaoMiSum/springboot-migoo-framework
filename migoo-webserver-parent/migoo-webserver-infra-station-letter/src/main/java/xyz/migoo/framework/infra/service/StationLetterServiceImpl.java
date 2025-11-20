package xyz.migoo.framework.infra.service;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.stationletter.vo.StationLetterPageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.StationLetterDO;
import xyz.migoo.framework.infra.dal.mapper.StationLetterMapper;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

import java.util.Collection;
import java.util.List;

@Service
public class StationLetterServiceImpl implements StationLetterService {

    @Resource
    private StationLetterMapper mapper;

    @Override
    public PageResult<StationLetterDO> getPage(StationLetterPageReqVO req) {
        return mapper.selectPage(req);
    }

    @Override
    public StationLetterDO get(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public Long add(String code, String title, String content, String fromUserType, Long toUserId, String toUserType) {
        StationLetterDO bean = new StationLetterDO().setCode(code).setTitle(title).setContent(content)
                .setToUserId(toUserId).setToUserType(toUserType)
                .setFromUserId(SecurityFrameworkUtils.getLoginUserId()).setFromUserType(fromUserType);
        mapper.insert(bean);
        return bean.getId();
    }

    @Override
    public void read(Collection<Long> ids) {
        update(ids, 0);
    }

    @Override
    public void unread(Collection<Long> ids) {
        update(ids, 1);
    }

    private void update(Collection<Long> ids, int flag) {
        List<StationLetterDO> entities = Lists.newArrayList();
        ids.forEach(id -> entities.add(new StationLetterDO().setUnread(flag).setId(id)));
        mapper.updateBatch(entities);
    }

    @Override
    public void remove(Collection<Long> ids) {
        mapper.deleteByIds(ids);
    }
}

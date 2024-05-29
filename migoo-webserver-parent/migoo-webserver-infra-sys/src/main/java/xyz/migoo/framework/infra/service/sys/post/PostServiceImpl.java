package xyz.migoo.framework.infra.service.sys.post;

import xyz.migoo.framework.infra.controller.sys.post.vo.PostQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Post;
import xyz.migoo.framework.infra.dal.mapper.sys.PostMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.enums.CommonStatusEnum;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static xyz.migoo.framework.infra.enums.SysErrorCodeConstants.POST_CODE_DUPLICATE;
import static xyz.migoo.framework.infra.enums.SysErrorCodeConstants.POST_NOT_FOUND;

@Service
public class PostServiceImpl implements PostService {

    @Resource
    private PostMapper mapper;

    @Override
    public PageResult<Post> getPage(PostQueryReqVO req) {
        return mapper.selectPage(req);
    }

    @Override
    public void verify(String code, String name, Long id) {
        if (Objects.nonNull(id)) {
            // id 非空 检查 id 是否正确
            if (Objects.isNull(mapper.selectById(id))) {
                throw ServiceExceptionUtil.get(POST_NOT_FOUND);
            }
            Post post = mapper.selectByCode(code);
            // code 有数据，且 id不一致
            if (Objects.nonNull(post) && !Objects.equals(post.getId(), id)) {
                throw ServiceExceptionUtil.get(POST_CODE_DUPLICATE);
            }
            post = mapper.selectByName(name);
            // name 有数据，且 id不一致
            if (Objects.nonNull(post) && !Objects.equals(post.getId(), id)) {
                throw ServiceExceptionUtil.get(POST_CODE_DUPLICATE);
            }
        } else {
            // id 为空
            Post post = mapper.selectByCode(code);
            if (Objects.nonNull(post)) {
                throw ServiceExceptionUtil.get(POST_CODE_DUPLICATE);
            }
            post = mapper.selectByName(name);
            if (Objects.nonNull(post)) {
                throw ServiceExceptionUtil.get(POST_CODE_DUPLICATE);
            }
        }
    }

    @Override
    public void add(Post post) {
        post.setStatus(CommonStatusEnum.ENABLE.getStatus());
        mapper.insert(post);
    }

    @Override
    public void update(Post post) {
        mapper.updateById(post);
    }

    @Override
    public Post get(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void remove(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public List<Post> getList(Collection<Long> ids, Collection<Integer> statuses) {
        return mapper.selectList(ids, statuses);
    }
}

package xyz.migoo.framework.infra.service.sys.post;

import xyz.migoo.framework.infra.controller.sys.post.vo.PostQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Post;
import xyz.migoo.framework.common.pojo.PageResult;

import java.util.Collection;
import java.util.List;

public interface PostService {
    PageResult<Post> getPage(PostQueryReqVO req);

    void verify(String code, String name, Long id);

    void add(Post post);

    void update(Post post);

    Post get(Long id);

    void remove(Long id);

    List<Post> getList(Collection<Long> ids, Collection<Integer> statuses);
}

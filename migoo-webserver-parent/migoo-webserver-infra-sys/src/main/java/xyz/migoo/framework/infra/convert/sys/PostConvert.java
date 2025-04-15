package xyz.migoo.framework.infra.convert.sys;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.SimpleData;
import xyz.migoo.framework.infra.controller.sys.post.vo.PostAddReqVO;
import xyz.migoo.framework.infra.controller.sys.post.vo.PostRespVO;
import xyz.migoo.framework.infra.controller.sys.post.vo.PostUpdateReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Post;

import java.util.List;

@Mapper
public interface PostConvert {

    PostConvert INSTANCE = Mappers.getMapper(PostConvert.class);

    PageResult<PostRespVO> convert(PageResult<Post> page);

    Post convert(PostAddReqVO req);

    Post convert(PostUpdateReqVO req);

    List<SimpleData> convert(List<Post> list);

    default SimpleData convert1(Post bean) {
        return new SimpleData(bean.getId(), bean.getName());
    }

    PostRespVO convert(Post post);
}

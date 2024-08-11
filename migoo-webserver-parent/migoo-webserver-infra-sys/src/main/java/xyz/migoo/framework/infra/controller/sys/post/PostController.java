package xyz.migoo.framework.infra.controller.sys.post;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.sys.post.vo.*;
import xyz.migoo.framework.infra.convert.sys.PostConvert;
import xyz.migoo.framework.infra.dal.dataobject.sys.Post;
import xyz.migoo.framework.infra.service.sys.post.PostService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static xyz.migoo.framework.common.enums.CommonStatus.enabled;

@RestController
@RequestMapping("/post")
public class PostController {

    @Resource
    private PostService postService;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('system:post:query')")
    public Result<PageResult<PostRespVO>> getPage(PostQueryReqVO req) {
        PageResult<PostRespVO> result = PostConvert.INSTANCE.convert(postService.getPage(req));
        result.getList().sort(Comparator.comparing(PostRespVO::getSort));
        return Result.getSuccessful(result);
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermission('system:post:add')")
    public Result<?> add(@RequestBody PostAddReqVO req) {
        postService.verify(req.getCode(), req.getName(), null);
        postService.add(PostConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('system:post:update')")
    public Result<?> update(@RequestBody PostUpdateReqVO req) {
        postService.verify(req.getCode(), req.getName(), req.getId());
        postService.update(PostConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:post:update')")
    public Result<?> get(@PathVariable("id") Long id) {
        return Result.getSuccessful(PostConvert.INSTANCE.convert(postService.get(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:post:remove')")
    public Result<?> remove(@PathVariable("id") Long id) {
        postService.remove(id);
        return Result.getSuccessful();
    }

    @GetMapping("/simple")
    public Result<List<PostSimpleRespVO>> getSimplePosts() {
        // 获得岗位列表，只要开启状态的
        List<Post> list = postService.getList(null, Collections.singleton(enabled.status()));
        // 排序后，返回给前端
        list.sort(Comparator.comparing(Post::getSort));
        return Result.getSuccessful(PostConvert.INSTANCE.convert(list));
    }
}

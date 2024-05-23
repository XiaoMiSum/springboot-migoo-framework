package xyz.migoo.framework.infra.controller.developer.dictionary;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.pojo.SimpleData;
import xyz.migoo.framework.infra.controller.developer.dictionary.vo.*;
import xyz.migoo.framework.infra.convert.developer.dictionary.DictionaryConvert;
import xyz.migoo.framework.infra.service.developer.dictionary.DictionaryService;
import xyz.migoo.framework.infra.service.developer.dictionary.DictionaryValueService;

import java.util.List;
import java.util.Map;

import static xyz.migoo.framework.common.enums.CommonStatusEnum.isEnabled;

@RestController
@RequestMapping("/developer/dictionary")
public class DictionaryController {

    @Resource
    private DictionaryService dictionaryService;
    @Resource
    private DictionaryValueService valueService;


    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:dictionary:query')")
    public Result<PageResult<?>> getPage(DictionaryPageReqVO req) {
        return Result.getSuccessful(DictionaryConvert.INSTANCE.convert(dictionaryService.get(req)));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermission('developer:dictionary:update')")
    public Result<?> add(@RequestBody DictionaryAddReqVO req) {
        dictionaryService.verify(req.getCode(), null);
        dictionaryService.add(DictionaryConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:dictionary:add')")
    public Result<?> update(@RequestBody DictionaryUpdateReqVO req) {
        dictionaryService.verify(req.getCode(), req.getId());
        dictionaryService.update(DictionaryConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:remove')")
    public Result<?> remove(@PathVariable("id") Long id) {
        dictionaryService.remove(id);
        return Result.getSuccessful();
    }

    @GetMapping("/simple")
    public Result<List<?>> getSimple() {
        List<SimpleData<String>> results = Lists.newArrayList();
        dictionaryService.get().forEach(item -> results.add(new SimpleData<>(item.getCode(), item.getName(), isEnabled(item.getStatus()))));
        return Result.getSuccessful(results);
    }

    @GetMapping("/value")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:query')")
    public Result<PageResult<?>> getPage(DictionaryValuePageReqVO req) {
        return Result.getSuccessful(DictionaryConvert.INSTANCE.convert2(valueService.get(req)));
    }

    @PostMapping("/value")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:add')")
    public Result<?> addValue(@RequestBody DictionaryValueAddReqVO req) {
        valueService.add(DictionaryConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @PutMapping("/value")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:update')")
    public Result<?> updateValue(@RequestBody DictionaryValueUpdateReqVO req) {
        valueService.update(DictionaryConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @DeleteMapping("/value/{id}")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:remove')")
    public Result<?> removeValue(@PathVariable("id") Long id) {
        valueService.remove(id);
        return Result.getSuccessful();
    }

    @GetMapping("/value/simple")
    public Result<List<?>> getValueSimple() {
        List<Map<String, Object>> results = Lists.newArrayList();
        valueService.get().forEach(item -> {
            Map<String, Object> map = Maps.newHashMap();
            results.add(map);
            map.put("dictCode", item.getDictCode());
            map.put("label", item.getLabel());
            map.put("value", item.getValue());
            map.put("disable", !isEnabled(item.getStatus()));
            map.put("colorType", item.getColorType());
        });
        return Result.getSuccessful(results);
    }
}

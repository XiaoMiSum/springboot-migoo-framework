package xyz.migoo.framework.infra.controller.developer.dictionary;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

import static xyz.migoo.framework.common.enums.CommonStatus.isDisabled;

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
        return Result.ok(DictionaryConvert.INSTANCE.convert(dictionaryService.get(req)));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermission('developer:dictionary:add')")
    public Result<?> add(@Valid @RequestBody DictionaryAddReqVO req) {
        dictionaryService.verify(req.getCode(), null);
        dictionaryService.add(DictionaryConvert.INSTANCE.convert(req));
        return Result.ok();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:dictionary:update')")
    public Result<?> update(@Valid @RequestBody DictionaryUpdateReqVO req) {
        dictionaryService.verify(req.getCode(), req.getId());
        dictionaryService.update(DictionaryConvert.INSTANCE.convert(req));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:remove')")
    public Result<?> remove(@PathVariable("id") Long id) {
        dictionaryService.remove(id);
        return Result.ok();
    }

    @GetMapping("/simple")
    public Result<List<?>> getSimple() {
        List<SimpleData> results = Lists.newArrayList();
        dictionaryService.get().forEach(item -> results.add(new SimpleData(item.getCode(), item.getName(), item.getStatus())));
        return Result.ok(results);
    }

    @GetMapping("/value")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:query')")
    public Result<PageResult<?>> getPage(DictionaryValuePageReqVO req) {
        return Result.ok(DictionaryConvert.INSTANCE.convert2(valueService.get(req)));
    }

    @PostMapping("/value")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:add')")
    public Result<?> addValue(@Valid @RequestBody DictionaryValueAddReqVO req) {
        valueService.add(DictionaryConvert.INSTANCE.convert(req));
        return Result.ok();
    }

    @PutMapping("/value")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:update')")
    public Result<?> updateValue(@Valid @RequestBody DictionaryValueUpdateReqVO req) {
        valueService.update(DictionaryConvert.INSTANCE.convert(req));
        return Result.ok();
    }

    @DeleteMapping("/value/{id}")
    @PreAuthorize("@ss.hasPermission('developer:dictionary:remove')")
    public Result<?> removeValue(@PathVariable("id") Long id) {
        valueService.remove(id);
        return Result.ok();
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
            map.put("disabled", isDisabled(item.getStatus()));
            map.put("colorType", item.getColorType());
        });
        return Result.ok(results);
    }
}

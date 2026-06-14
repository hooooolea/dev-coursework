package com.police.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.common.result.Result;
import com.police.system.entity.SysDict;
import com.police.system.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final SysDictMapper dictMapper;

    /**
     * 按字典类型查询
     */
    @GetMapping("/{type}")
    public Result<List<SysDict>> getByType(@PathVariable String type) {
        List<SysDict> list = dictMapper.selectList(
                new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getDictType, type)
                        .eq(SysDict::getStatus, 1)
                        .orderByAsc(SysDict::getSortOrder));
        return Result.ok(list);
    }
}

package com.police.person.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.common.annotation.OperationLog;
import com.police.common.config.FileStorageConfig;
import com.police.common.exception.BusinessException;
import com.police.common.result.Result;
import com.police.common.util.FileStorageService;
import com.police.person.entity.PersonInfo;
import com.police.person.entity.PersonViolation;
import com.police.person.service.impl.PersonServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonServiceImpl personService;
    private final FileStorageService fileStorageService;
    private final FileStorageConfig fileStorageConfig;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('person:view')")
    public Result<IPage<PersonInfo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String personType) {
        return Result.ok(personService.listPage(page, size, keyword, personType));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('person:view')")
    public Result<PersonInfo> detail(@PathVariable Long id) {
        return Result.ok(personService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('person:add')")
    @OperationLog(module = "人员档案", action = "新增人员")
    public Result<Long> create(@RequestBody PersonInfo person) {
        return Result.ok(personService.create(person));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('person:edit')")
    @OperationLog(module = "人员档案", action = "编辑人员")
    public Result<?> update(@PathVariable Long id, @RequestBody PersonInfo person) {
        person.setId(id);
        personService.updateById(person);
        return Result.ok();
    }

    @PostMapping("/{id}/label")
    @PreAuthorize("hasAuthority('person:edit')")
    @OperationLog(module = "人员档案", action = "标注人员类型")
    public Result<?> label(@PathVariable Long id, @RequestBody Map<String, String> body) {
        personService.labelType(id, body.get("type"), body.get("reason"));
        return Result.ok();
    }

    @GetMapping("/{id}/violations")
    @PreAuthorize("hasAuthority('person:view')")
    public Result<List<PersonViolation>> violations(@PathVariable Long id) {
        return Result.ok(personService.listViolations(id));
    }

    @PostMapping("/{id}/violations")
    @PreAuthorize("hasAuthority('person:edit')")
    @OperationLog(module = "人员档案", action = "新增违法记录")
    public Result<?> addViolation(@PathVariable Long id, @RequestBody PersonViolation violation) {
        personService.addViolation(id, violation);
        return Result.ok();
    }

    /**
     * 人员头像上传（multipart），落盘后更新 person_info.photo_url。
     *
     * <p>限制：仅允许 jpg/jpeg/png/gif/webp，单文件 ≤ 2MB（由 {@link FileStorageConfig} 配置）。</p>
     */
    @PostMapping("/{id}/avatar")
    @PreAuthorize("hasAuthority('person:edit')")
    @OperationLog(module = "人员档案", action = "上传头像")
    public Result<Map<String, Object>> uploadAvatar(@PathVariable Long id,
                                                    @RequestParam("file") MultipartFile file) {
        PersonInfo existing = personService.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "人员不存在");
        }
        FileStorageService.StoredFile stored = fileStorageService.store(
                file, "person/" + id + "/avatar",
                fileStorageConfig.getAvatarAllowTypes(),
                fileStorageConfig.getAvatarMaxSize());

        // 更新 photo_url
        PersonInfo update = new PersonInfo();
        update.setId(id);
        update.setPhotoUrl(stored.getAccessUrl());
        personService.updateById(update);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("photoUrl", stored.getAccessUrl());
        data.put("fileName", stored.getOriginalName());
        data.put("fileSize", stored.getSize());
        data.put("fileType", stored.getExt());
        return Result.ok("上传成功", data);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('person:del')")
    @OperationLog(module = "人员档案", action = "删除人员")
    public Result<?> delete(@PathVariable Long id) {
        personService.removeById(id);
        return Result.ok();
    }
}

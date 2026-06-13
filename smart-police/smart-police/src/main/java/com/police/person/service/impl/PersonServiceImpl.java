package com.police.person.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.common.exception.BusinessException;
import com.police.common.util.SecurityUtil;
import com.police.person.entity.PersonInfo;
import com.police.person.entity.PersonViolation;
import com.police.person.mapper.PersonInfoMapper;
import com.police.person.mapper.PersonViolationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PersonServiceImpl extends ServiceImpl<PersonInfoMapper, PersonInfo> {

    private final PersonViolationMapper violationMapper;

    public IPage<PersonInfo> listPage(int page, int size, String keyword, String personType) {
        return baseMapper.selectPersonPage(new Page<>(page, size), keyword, personType);
    }

    public Long create(PersonInfo person) {
        // 校验身份证唯一
        if (person.getIdCard() != null) {
            long cnt = count(new LambdaQueryWrapper<PersonInfo>()
                    .eq(PersonInfo::getIdCard, person.getIdCard()));
            if (cnt > 0) throw BusinessException.of("该身份证号已录入系统");
            // 存后4位用于检索
            String raw = person.getIdCard();
            person.setIdCardTail(raw.length() >= 4 ? raw.substring(raw.length() - 4) : raw);
        }
        if (person.getPhone() != null && person.getPhone().length() >= 4) {
            person.setPhoneTail(person.getPhone().substring(person.getPhone().length() - 4));
        }
        person.setPersonType("normal");
        save(person);
        return person.getId();
    }

    public void labelType(Long personId, String type, String reason) {
        PersonInfo person = getById(personId);
        if (person == null) throw BusinessException.of("人员不存在");
        person.setPersonType(type);
        person.setTypeReason(reason);
        person.setTypeOperatorId(SecurityUtil.getCurrentUserId());
        person.setTypeAt(LocalDateTime.now());
        updateById(person);
    }

    public void addViolation(Long personId, PersonViolation violation) {
        if (getById(personId) == null) throw BusinessException.of("人员不存在");
        violation.setPersonId(personId);
        violationMapper.insert(violation);
    }

    public List<PersonViolation> listViolations(Long personId) {
        return violationMapper.selectList(
                new LambdaQueryWrapper<PersonViolation>()
                        .eq(PersonViolation::getPersonId, personId)
                        .orderByDesc(PersonViolation::getViolationTime));
    }
}

package com.police.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.equipment.entity.EquipmentBorrow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EquipmentBorrowMapper extends BaseMapper<EquipmentBorrow> {

    @Select("""
            SELECT eb.*, ei.equip_name, ei.equip_no, o.real_name AS borrower_name
            FROM equipment_borrow eb
            LEFT JOIN equipment_info ei ON eb.equipment_id = ei.id
            LEFT JOIN officer_info o    ON eb.borrower_id  = o.id
            WHERE eb.equipment_id = #{equipmentId}
            ORDER BY eb.borrow_time DESC
            """)
    List<EquipmentBorrow> selectByEquipmentId(@Param("equipmentId") Long equipmentId);
}

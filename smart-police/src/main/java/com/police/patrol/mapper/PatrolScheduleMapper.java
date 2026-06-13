package com.police.patrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.patrol.entity.PatrolSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PatrolScheduleMapper extends BaseMapper<PatrolSchedule> {

    /** 按日期范围查询，联查警员姓名和警号 */
    @Select("""
            SELECT ps.*, o.real_name AS officer_name, o.badge_no
            FROM patrol_schedule ps
            LEFT JOIN officer_info o ON ps.officer_id = o.id
            WHERE ps.schedule_date BETWEEN #{startDate} AND #{endDate}
            ORDER BY ps.schedule_date, ps.shift_type, o.badge_no
            """)
    List<PatrolSchedule> selectByDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate")   LocalDate endDate);
}

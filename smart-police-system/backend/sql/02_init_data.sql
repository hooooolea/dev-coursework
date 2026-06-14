USE smart_police;

-- ===================== 部门 =====================
INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, sort_order) VALUES
(1, 0, '某市公安局', 'PSJ000', 1),
(2, 1, '刑侦支队',   'PSJ001', 1),
(3, 1, '治安支队',   'PSJ002', 2),
(4, 1, '交管支队',   'PSJ003', 3),
(5, 1, '城东派出所', 'PSJ101', 4),
(6, 1, '城西派出所', 'PSJ102', 5);

-- ===================== 角色 =====================
INSERT INTO sys_role (id, role_code, role_name, description, sort_order) VALUES
(1, 'ROLE_SUPER_ADMIN', '超级管理员', '系统最高权限', 1),
(2, 'ROLE_DIRECTOR',    '局长/所长',  '全局数据查阅', 2),
(3, 'ROLE_OFFICER',     '民警',       '日常业务操作', 3),
(4, 'ROLE_AUXILIARY',   '辅警',       '辅助录入权限', 4),
(5, 'ROLE_DUTY',        '值班员',     '报警受理权限', 5);

-- ===================== 权限 =====================
INSERT INTO sys_permission (id, parent_id, perm_type, perm_code, perm_name, sort_order) VALUES
-- 系统管理
(1,  0, 1, 'sys',              '系统管理',     1),
(2,  1, 2, 'sys:user:view',    '用户查询',     1),
(3,  1, 2, 'sys:user:add',     '用户新增',     2),
(4,  1, 2, 'sys:user:edit',    '用户编辑',     3),
(5,  1, 2, 'sys:user:delete',  '用户删除',     4),
-- 报警受理
(10, 0, 1, 'alarm',            '报警受理',     2),
(11,10, 2, 'alarm:view',       '警情查询',     1),
(12,10, 2, 'alarm:add',        '接警录入',     2),
(13,10, 2, 'alarm:dispatch',   '任务派发',     3),
(14,10, 2, 'alarm:close',      '关闭警情',     4),
-- 案件管理
(20, 0, 1, 'case',             '案件管理',     3),
(21,20, 2, 'case:view',        '案件查询',     1),
(22,20, 2, 'case:add',         '案件立案',     2),
(23,20, 2, 'case:edit',        '案件编辑',     3),
(24,20, 2, 'case:delete',      '案件删除',     4),
(25,20, 2, 'case:export',      '案件导出',     5),
-- 人员档案
(30, 0, 1, 'person',           '人员档案',     4),
(31,30, 2, 'person:view',      '人员查询',     1),
(32,30, 2, 'person:add',       '人员新增',     2),
(33,30, 2, 'person:edit',      '人员编辑',     3),
-- 车辆管理
(40, 0, 1, 'vehicle',          '车辆管理',     5),
(41,40, 2, 'vehicle:view',     '车辆查询',     1),
(42,40, 2, 'vehicle:add',      '车辆新增',     2),
(43,40, 2, 'vehicle:edit',     '车辆编辑',     3),
(44,40, 2, 'vehicle:control',  '布控管理',     4),
-- 巡逻调度
(50, 0, 1, 'patrol',           '巡逻调度',     6),
(51,50, 2, 'patrol:view',      '排班查询',     1),
(52,50, 2, 'patrol:manage',    '排班管理',     2),
-- 统计分析
(60, 0, 1, 'stat',             '统计分析',     7),
(61,60, 2, 'stat:view',        '统计查询',     1),
(62,60, 2, 'stat:export',      '统计导出',     2);

-- ===================== 超级管理员账号 =====================
-- 密码: 123456  (BCrypt 加密，hash 由 Spring Security BCryptPasswordEncoder 生成，强度 10)
INSERT INTO sys_user (id, badge_no, username, password, real_name, gender, dept_id, status, login_fail_count) VALUES
(1, 'ADMIN000', 'admin',
 '$2a$10$CHYsMjJ0/yPTkSCs7PbeR.jXZHD.pYafinM1RennpmMUp0smNGK76',
 '系统管理员', 1, 1, 1, 0);

-- 超管分配超管角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 超管角色拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE is_deleted = 0;

-- ===================== 数据字典 =====================
INSERT INTO sys_dict (dict_type, dict_label, dict_value, sort_order) VALUES
-- 案件大类
('case_category', '刑事案件', 'criminal',  1),
('case_category', '治安案件', 'public',    2),
('case_category', '交通案件', 'traffic',   3),
('case_category', '行政案件', 'admin',     4),
-- 案件小类
('case_type', '盗窃罪',   'theft',       1),
('case_type', '诈骗罪',   'fraud',       2),
('case_type', '抢劫罪',   'robbery',     3),
('case_type', '故意伤害', 'injury',      4),
('case_type', '交通肇事', 'traffic_acc', 5),
('case_type', '寻衅滋事', 'disturb',     6),
('case_type', '其他案件', 'other',      99),
-- 案件状态
('case_status', '待立案',   'pending',       1),
('case_status', '侦查中',   'investigating', 2),
('case_status', '已移送',   'transferred',  3),
('case_status', '已结案',   'closed',       4),
('case_status', '已撤案',   'cancelled',    5),
-- 紧急程度
('urgency_level', '一般',   '1', 1),
('urgency_level', '较紧急', '2', 2),
('urgency_level', '紧急',   '3', 3),
('urgency_level', '特急',   '4', 4),
-- 警情类型
('alarm_type', '盗窃警情', 'theft',    1),
('alarm_type', '斗殴警情', 'fight',    2),
('alarm_type', '诈骗警情', 'fraud',    3),
('alarm_type', '交通事故', 'traffic',  4),
('alarm_type', '火灾警情', 'fire',     5),
('alarm_type', '其他警情', 'other',   99),
-- 人员类型
('person_type', '普通人员', 'normal',     1),
('person_type', '重点关注', 'focus',      2),
('person_type', '在逃人员', 'wanted',     3),
('person_type', '社区矫正', 'correction', 4),
('person_type', '涉毒人员', 'drug',       5),
-- 车辆类型
('vehicle_type', '轿车',   'sedan',    1),
('vehicle_type', 'SUV',    'suv',      2),
('vehicle_type', '货车',   'truck',    3),
('vehicle_type', '摩托车', 'moto',     4),
('vehicle_type', '其他',   'other',   99),
-- 装备类型
('equipment_type', '枪支弹药', 'weapon',     1),
('equipment_type', '警用车辆', 'vehicle',    2),
('equipment_type', '通信设备', 'comm',       3),
('equipment_type', '防护装备', 'protection', 4),
('equipment_type', '办公设备', 'office',     5),
-- 巡逻班次
('shift_type', '早班', 'morning',   1),
('shift_type', '中班', 'afternoon', 2),
('shift_type', '夜班', 'night',     3);

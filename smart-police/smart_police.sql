-- =============================================================
-- 智能警务管理系统 - 完整建表脚本
-- 生成时间: 2026-06-10
-- 数据库: MySQL 8.0+  字符集: utf8mb4
-- =============================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_police
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_police;

-- 关闭外键检查（导入时）
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE sys_user (
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    badge_no      VARCHAR(20)      NOT NULL COMMENT '警号（唯一）',
    username      VARCHAR(50)      NOT NULL COMMENT '登录用户名',
    password      VARCHAR(255)     NOT NULL COMMENT 'BCrypt加密密码',
    real_name     VARCHAR(50)      NOT NULL COMMENT '真实姓名',
    gender        TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '性别 1男2女',
    phone         VARCHAR(20)               COMMENT '手机号（AES加密存储）',
    email         VARCHAR(100)              COMMENT '邮箱',
    dept_id       BIGINT UNSIGNED           COMMENT '所属部门ID',
    avatar        VARCHAR(500)              COMMENT '头像URL',
    status        TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '账号状态 1正常 0禁用',
    login_fail_count INT            NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    lock_until    DATETIME                  COMMENT '账号锁定到期时间',
    last_login_at DATETIME                  COMMENT '最后登录时间',
    last_login_ip VARCHAR(45)               COMMENT '最后登录IP',
    remark        VARCHAR(500)              COMMENT '备注',
    created_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by    BIGINT UNSIGNED           COMMENT '创建人ID',
    is_deleted    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_badge_no (badge_no),
    UNIQUE KEY uk_username (username),
    KEY idx_dept_id (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE sys_role (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_code   VARCHAR(50)      NOT NULL COMMENT '角色编码，如 ROLE_OFFICER',
    role_name   VARCHAR(50)      NOT NULL COMMENT '角色名称',
    description VARCHAR(200)              COMMENT '描述',
    sort_order  INT              NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  BIGINT UNSIGNED,
    is_deleted  TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE sys_user_role (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id    BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT UNSIGNED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE sys_permission (
    id             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    parent_id      BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '父权限ID，0为根节点',
    perm_type      TINYINT(1)       NOT NULL COMMENT '权限类型 1菜单 2按钮/接口',
    perm_code      VARCHAR(100)     NOT NULL COMMENT '权限编码，如 case:edit',
    perm_name      VARCHAR(100)     NOT NULL COMMENT '权限名称',
    icon           VARCHAR(100)              COMMENT '菜单图标',
    path           VARCHAR(200)              COMMENT '菜单路径/接口路径',
    component      VARCHAR(200)              COMMENT '前端组件路径',
    sort_order     INT              NOT NULL DEFAULT 0,
    status         TINYINT(1)       NOT NULL DEFAULT 1,
    created_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted     TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE sys_role_permission (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    role_id       BIGINT UNSIGNED NOT NULL,
    permission_id BIGINT UNSIGNED NOT NULL,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE sys_dept (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '父部门ID',
    dept_name   VARCHAR(100)     NOT NULL COMMENT '部门名称',
    dept_code   VARCHAR(50)               COMMENT '部门编码',
    leader_id   BIGINT UNSIGNED           COMMENT '负责人ID',
    phone       VARCHAR(20)               COMMENT '联系电话',
    address     VARCHAR(200)              COMMENT '地址',
    sort_order  INT              NOT NULL DEFAULT 0,
    status      TINYINT(1)       NOT NULL DEFAULT 1,
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE sys_dict (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    dict_type   VARCHAR(50)      NOT NULL COMMENT '字典类型，如 case_type',
    dict_label  VARCHAR(100)     NOT NULL COMMENT '字典标签，如 刑事案件',
    dict_value  VARCHAR(100)     NOT NULL COMMENT '字典值，如 criminal',
    sort_order  INT              NOT NULL DEFAULT 0,
    status      TINYINT(1)       NOT NULL DEFAULT 1,
    remark      VARCHAR(300),
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

CREATE TABLE sys_operation_log (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    user_id      BIGINT UNSIGNED           COMMENT '操作人ID',
    user_name    VARCHAR(50)               COMMENT '操作人姓名（冗余）',
    module       VARCHAR(100)              COMMENT '模块名称',
    action       VARCHAR(100)              COMMENT '操作动作',
    method       VARCHAR(200)              COMMENT '请求方法全路径',
    request_url  VARCHAR(500)              COMMENT '请求URL',
    request_ip   VARCHAR(45)               COMMENT '请求IP',
    request_body TEXT                      COMMENT '请求体（脱敏处理）',
    response_code INT                      COMMENT 'HTTP响应码',
    execute_time INT                       COMMENT '执行耗时（毫秒）',
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at),
    KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE alarm_record (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    alarm_no        VARCHAR(30)      NOT NULL COMMENT '警情编号，如 BJ20240601001',
    alarm_time      DATETIME         NOT NULL COMMENT '报警时间',
    caller_name     VARCHAR(50)               COMMENT '报警人姓名',
    caller_phone    VARCHAR(20)               COMMENT '报警人电话（加密）',
    location_province VARCHAR(20)            COMMENT '事发省份',
    location_city   VARCHAR(20)              COMMENT '事发城市',
    location_district VARCHAR(20)            COMMENT '事发区县',
    location_detail VARCHAR(200)             COMMENT '详细地址',
    alarm_type      VARCHAR(50)      NOT NULL COMMENT '警情类型（字典值）',
    alarm_desc      TEXT                      COMMENT '警情描述',
    urgency_level   TINYINT(1)       NOT NULL DEFAULT 2 COMMENT '紧急程度 1一般 2较紧急 3紧急 4特急',
    status          TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '状态 1待处置 2处置中 3已处置 4已关闭',
    duty_user_id    BIGINT UNSIGNED           COMMENT '接警值班员ID',
    related_case_id BIGINT UNSIGNED           COMMENT '关联案件ID',
    close_time      DATETIME                  COMMENT '关闭时间',
    close_summary   TEXT                      COMMENT '处置结果摘要',
    attachment_url  VARCHAR(500)              COMMENT '录音附件URL',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    is_deleted      TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_alarm_no (alarm_no),
    KEY idx_alarm_time (alarm_time),
    KEY idx_status (status),
    KEY idx_urgency (urgency_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警记录表';

CREATE TABLE alarm_dispatch (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    alarm_id        BIGINT UNSIGNED  NOT NULL COMMENT '警情ID',
    officer_id      BIGINT UNSIGNED  NOT NULL COMMENT '处置警员ID',
    dispatch_time   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '派发时间',
    expected_arrive DATETIME                  COMMENT '预计到达时间',
    arrive_time     DATETIME                  COMMENT '实际到达时间',
    status          TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '1待接收 2已接收 3处置中 4已完成',
    result_desc     TEXT                      COMMENT '处置结果描述',
    complete_time   DATETIME                  COMMENT '完成时间',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_alarm_id (alarm_id),
    KEY idx_officer_id (officer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='警情派发表';

CREATE TABLE case_info (
    id                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    case_no             VARCHAR(30)      NOT NULL COMMENT '案件编号，如 AJ20240601001',
    case_name           VARCHAR(200)     NOT NULL COMMENT '案件名称',
    case_category       VARCHAR(50)      NOT NULL COMMENT '案件大类 criminal刑事 public治安 traffic交通',
    case_type           VARCHAR(50)      NOT NULL COMMENT '案件小类（字典值）',
    occurred_at         DATETIME         NOT NULL COMMENT '发案时间',
    location_province   VARCHAR(20),
    location_city       VARCHAR(20),
    location_district   VARCHAR(20),
    location_detail     VARCHAR(200)              COMMENT '详细地址',
    case_desc           TEXT             NOT NULL COMMENT '案情描述',
    status              VARCHAR(20)      NOT NULL DEFAULT 'pending' COMMENT 'pending待立案 investigating侦查中 transferred已移送 closed已结案 cancelled撤案',
    severity_level      TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '案件等级 1一般 2重要 3重大 4特重大',
    lead_officer_id     BIGINT UNSIGNED           COMMENT '主办民警ID',
    dept_id             BIGINT UNSIGNED           COMMENT '负责部门ID',
    file_date           DATE                      COMMENT '立案日期',
    closed_date         DATE                      COMMENT '结案日期',
    cancelled_reason    VARCHAR(500)              COMMENT '撤案原因',
    deadline_date       DATE                      COMMENT '办案期限',
    is_overdue          TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否超期',
    related_alarm_id    BIGINT UNSIGNED           COMMENT '关联警情ID',
    remark              VARCHAR(500),
    created_at          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by          BIGINT UNSIGNED,
    is_deleted          TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_no (case_no),
    KEY idx_status (status),
    KEY idx_lead_officer (lead_officer_id),
    KEY idx_file_date (file_date),
    KEY idx_case_type (case_type),
    KEY idx_severity (severity_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件信息表';

CREATE TABLE case_officer (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id    BIGINT UNSIGNED NOT NULL COMMENT '案件ID',
    officer_id BIGINT UNSIGNED NOT NULL COMMENT '警员ID',
    role_type  VARCHAR(20)     NOT NULL DEFAULT 'member' COMMENT 'lead主办 member协办',
    join_date  DATE,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_officer (case_id, officer_id),
    KEY idx_officer_id (officer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件参与警员表';

CREATE TABLE case_progress (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    case_id      BIGINT UNSIGNED  NOT NULL COMMENT '案件ID',
    progress_time DATETIME        NOT NULL COMMENT '进展时间',
    content      TEXT             NOT NULL COMMENT '进展内容',
    next_plan    TEXT                      COMMENT '下一步计划',
    officer_id   BIGINT UNSIGNED           COMMENT '记录人ID',
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_case_id (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件侦查进展表';

CREATE TABLE case_status_log (
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    case_id       BIGINT UNSIGNED  NOT NULL,
    from_status   VARCHAR(20)               COMMENT '变更前状态',
    to_status     VARCHAR(20)      NOT NULL COMMENT '变更后状态',
    change_reason VARCHAR(500)              COMMENT '变更原因',
    operator_id   BIGINT UNSIGNED           COMMENT '操作人ID',
    created_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_case_id (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件状态变更日志';

CREATE TABLE case_evidence (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    case_id         BIGINT UNSIGNED  NOT NULL COMMENT '案件ID',
    evidence_no     VARCHAR(50)      NOT NULL COMMENT '证据编号',
    evidence_name   VARCHAR(200)     NOT NULL COMMENT '证据名称',
    evidence_type   VARCHAR(50)      NOT NULL COMMENT '证据类型（字典值）',
    collect_time    DATETIME                  COMMENT '收集时间',
    collect_location VARCHAR(200)             COMMENT '收集地点',
    collector_id    BIGINT UNSIGNED           COMMENT '收集人ID',
    storage_location VARCHAR(200)             COMMENT '存放位置',
    description     TEXT                      COMMENT '证据描述',
    file_url        VARCHAR(500)              COMMENT '附件URL',
    file_type       VARCHAR(20)               COMMENT '附件类型 image/video/document',
    file_size       BIGINT                    COMMENT '文件大小（字节）',
    status          TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '1正常 2已销毁',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    is_deleted      TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_evidence_no (evidence_no),
    KEY idx_case_id (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件证据表';

CREATE TABLE case_suspect (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    case_id          BIGINT UNSIGNED  NOT NULL COMMENT '案件ID',
    person_id        BIGINT UNSIGNED           COMMENT '人员档案ID（如已建档）',
    name             VARCHAR(50)      NOT NULL COMMENT '姓名',
    id_card          VARCHAR(100)              COMMENT '身份证号（加密）',
    gender           TINYINT(1),
    phone            VARCHAR(20)               COMMENT '联系方式（加密）',
    address          VARCHAR(200),
    suspect_level    TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '1嫌疑人 2重要嫌疑人 3已确认',
    interrogation_summary TEXT                 COMMENT '审讯摘要',
    arrest_status    TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '0在逃 1已抓获 2已羁押',
    created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by       BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_case_id (case_id),
    KEY idx_person_id (person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件嫌疑人表';

CREATE TABLE person_info (
    id                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    name              VARCHAR(50)      NOT NULL COMMENT '姓名',
    gender            TINYINT(1)       NOT NULL COMMENT '1男 2女',
    ethnicity         VARCHAR(20)               COMMENT '民族',
    id_card           VARCHAR(255)     NOT NULL COMMENT '身份证号（AES加密）',
    id_card_tail      VARCHAR(4)                COMMENT '身份证后4位（明文，用于模糊查询）',
    birth_date        DATE                      COMMENT '出生日期',
    phone             VARCHAR(255)              COMMENT '手机号（AES加密）',
    phone_tail        VARCHAR(4)                COMMENT '手机号后4位',
    household_addr    VARCHAR(300)              COMMENT '户籍地址',
    current_addr      VARCHAR(300)              COMMENT '现居住地址',
    occupation        VARCHAR(100)              COMMENT '职业',
    work_unit         VARCHAR(200)              COMMENT '工作单位',
    photo_url         VARCHAR(500)              COMMENT '照片URL',
    person_type       VARCHAR(20)      NOT NULL DEFAULT 'normal' COMMENT 'normal普通 focus重点关注 wanted在逃 correction社区矫正 drug涉毒',
    type_reason       VARCHAR(500)              COMMENT '标注原因',
    type_operator_id  BIGINT UNSIGNED           COMMENT '标注人',
    type_at           DATETIME                  COMMENT '标注时间',
    remark            TEXT,
    created_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        BIGINT UNSIGNED,
    dept_id           BIGINT UNSIGNED           COMMENT '录入部门',
    is_deleted        TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_id_card (id_card),
    KEY idx_name (name),
    KEY idx_person_type (person_type),
    KEY idx_id_card_tail (id_card_tail)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员档案表';

CREATE TABLE person_violation (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    person_id       BIGINT UNSIGNED  NOT NULL COMMENT '人员ID',
    violation_time  DATETIME         NOT NULL COMMENT '违法时间',
    violation_type  VARCHAR(50)      NOT NULL COMMENT '违法类型（字典值）',
    description     TEXT                      COMMENT '违法描述',
    result          VARCHAR(50)               COMMENT '处理结果（字典值）',
    fine_amount     DECIMAL(10, 2)            COMMENT '罚款金额',
    detention_days  INT                       COMMENT '拘留天数',
    related_case_id BIGINT UNSIGNED           COMMENT '关联案件ID',
    handler_id      BIGINT UNSIGNED           COMMENT '经办民警ID',
    dept_id         BIGINT UNSIGNED           COMMENT '处理部门',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_person_id (person_id),
    KEY idx_violation_time (violation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员违法记录表';

CREATE TABLE vehicle_info (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    plate_no        VARCHAR(20)      NOT NULL COMMENT '车牌号',
    vin             VARCHAR(50)               COMMENT '车架号（VIN）',
    vehicle_type    VARCHAR(20)      NOT NULL COMMENT '车辆类型（字典值）',
    brand           VARCHAR(50)               COMMENT '品牌',
    model           VARCHAR(100)              COMMENT '型号',
    color           VARCHAR(20)               COMMENT '颜色',
    year            INT                       COMMENT '年款',
    owner_person_id BIGINT UNSIGNED           COMMENT '登记人员ID',
    owner_name      VARCHAR(50)               COMMENT '登记人姓名（冗余）',
    register_date   DATE                      COMMENT '登记日期',
    expire_date     DATE                      COMMENT '证件有效期',
    photo_url       VARCHAR(500)              COMMENT '车辆照片',
    status          VARCHAR(20)      NOT NULL DEFAULT 'normal' COMMENT 'normal正常 suspect可疑 controlled布控 seized已扣押',
    remark          VARCHAR(500),
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    is_deleted      TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_plate_no (plate_no),
    KEY idx_status (status),
    KEY idx_owner (owner_person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆档案表';

CREATE TABLE vehicle_violation (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    vehicle_id       BIGINT UNSIGNED  NOT NULL COMMENT '车辆ID',
    violation_time   DATETIME         NOT NULL COMMENT '违章时间',
    violation_type   VARCHAR(50)      NOT NULL COMMENT '违章类型（字典值）',
    location         VARCHAR(200)              COMMENT '违章地点',
    fine_amount      DECIMAL(10, 2)            COMMENT '罚款金额',
    deducted_points  INT              NOT NULL DEFAULT 0 COMMENT '扣分',
    is_paid          TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '是否已缴款',
    paid_at          DATETIME                  COMMENT '缴款时间',
    related_case_id  BIGINT UNSIGNED           COMMENT '关联案件（如交通事故）',
    evidence_url     VARCHAR(500)              COMMENT '违章证据图片',
    handler_id       BIGINT UNSIGNED           COMMENT '处理民警',
    created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_violation_time (violation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆违章表';

CREATE TABLE vehicle_control (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    vehicle_id      BIGINT UNSIGNED  NOT NULL COMMENT '车辆ID',
    control_reason  VARCHAR(500)     NOT NULL COMMENT '布控原因',
    control_level   TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '1一般 2紧急',
    start_time      DATETIME         NOT NULL COMMENT '布控开始时间',
    end_time        DATETIME                  COMMENT '布控结束时间',
    related_case_id BIGINT UNSIGNED           COMMENT '关联案件',
    status          TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '1布控中 0已解控',
    decontrol_time  DATETIME                  COMMENT '解控时间',
    decontrol_reason VARCHAR(300)              COMMENT '解控原因',
    operator_id     BIGINT UNSIGNED           COMMENT '操作人',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆布控表';

CREATE TABLE patrol_schedule (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    officer_id   BIGINT UNSIGNED  NOT NULL COMMENT '警员ID',
    schedule_date DATE            NOT NULL COMMENT '排班日期',
    shift_type   VARCHAR(10)      NOT NULL COMMENT 'morning早班 afternoon中班 night夜班',
    start_time   TIME             NOT NULL COMMENT '班次开始时间',
    end_time     TIME             NOT NULL COMMENT '班次结束时间',
    dept_id      BIGINT UNSIGNED           COMMENT '所属部门',
    status       VARCHAR(10)      NOT NULL DEFAULT 'normal' COMMENT 'normal正常 adjusted已调整 leave请假',
    remark       VARCHAR(200),
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by   BIGINT UNSIGNED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_officer_date_shift (officer_id, schedule_date, shift_type),
    KEY idx_schedule_date (schedule_date),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡逻排班表';

CREATE TABLE patrol_task (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    task_no         VARCHAR(30)      NOT NULL COMMENT '任务编号',
    task_type       VARCHAR(20)      NOT NULL COMMENT 'routine例行 special专项 fixed定点',
    task_name       VARCHAR(200)     NOT NULL COMMENT '任务名称',
    officer_id      BIGINT UNSIGNED  NOT NULL COMMENT '负责警员ID',
    area_name       VARCHAR(100)              COMMENT '巡逻区域名称',
    task_start      DATETIME         NOT NULL COMMENT '计划开始时间',
    task_end        DATETIME         NOT NULL COMMENT '计划结束时间',
    status          VARCHAR(20)      NOT NULL DEFAULT 'pending' COMMENT 'pending待接收 accepted已接收 ongoing进行中 completed已完成 cancelled已取消',
    accept_time     DATETIME                  COMMENT '接收时间',
    complete_time   DATETIME                  COMMENT '完成时间',
    summary         TEXT                      COMMENT '巡逻小结',
    dispatch_by     BIGINT UNSIGNED           COMMENT '派发人ID',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_officer_id (officer_id),
    KEY idx_status (status),
    KEY idx_task_start (task_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡逻任务表';

CREATE TABLE patrol_checkin (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    task_id      BIGINT UNSIGNED  NOT NULL COMMENT '任务ID',
    officer_id   BIGINT UNSIGNED  NOT NULL COMMENT '警员ID',
    checkin_type VARCHAR(10)      NOT NULL COMMENT 'start开始 end结束 mid中途汇报',
    checkin_time DATETIME         NOT NULL COMMENT '打卡时间',
    longitude    DECIMAL(10, 7)            COMMENT '经度（GPS）',
    latitude     DECIMAL(10, 7)            COMMENT '纬度（GPS）',
    location_desc VARCHAR(200)             COMMENT '位置描述',
    note         VARCHAR(500)              COMMENT '情况说明',
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_officer_id (officer_id),
    KEY idx_checkin_time (checkin_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡逻打卡签到表';

CREATE TABLE officer_info (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED  NOT NULL COMMENT '关联sys_user',
    badge_no        VARCHAR(20)      NOT NULL COMMENT '警号',
    real_name       VARCHAR(50)      NOT NULL,
    gender          TINYINT(1)       NOT NULL,
    police_category VARCHAR(20)               COMMENT '警察类别（字典值）',
    rank_title      VARCHAR(30)               COMMENT '警衔',
    position        VARCHAR(50)               COMMENT '职务',
    dept_id         BIGINT UNSIGNED           COMMENT '所属部门',
    entry_date      DATE                      COMMENT '入职日期',
    id_card         VARCHAR(255)              COMMENT '身份证号（加密）',
    phone           VARCHAR(255)              COMMENT '手机号（加密）',
    emergency_contact VARCHAR(50)             COMMENT '紧急联系人',
    emergency_phone VARCHAR(255)              COMMENT '紧急联系人电话（加密）',
    skills          VARCHAR(500)              COMMENT '擅长领域（逗号分隔）',
    work_status     VARCHAR(20)      NOT NULL DEFAULT 'on_duty' COMMENT 'on_duty在岗 vacation休假 business外出公干 suspended停职',
    photo_url       VARCHAR(500),
    remark          TEXT,
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    is_deleted      TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_badge_no (badge_no),
    KEY idx_dept_id (dept_id),
    KEY idx_work_status (work_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='警员档案表';

CREATE TABLE officer_assessment (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    officer_id       BIGINT UNSIGNED  NOT NULL COMMENT '警员ID',
    period_type      VARCHAR(10)      NOT NULL COMMENT 'month月度 quarter季度 year年度',
    period_value     VARCHAR(10)      NOT NULL COMMENT '考核期间，如 2024-01、2024-Q1、2024',
    attendance_score DECIMAL(5, 2)             COMMENT '出勤得分',
    case_score       DECIMAL(5, 2)             COMMENT '案件参与得分',
    violation_score  DECIMAL(5, 2)             COMMENT '违规扣分',
    reward_score     DECIMAL(5, 2)             COMMENT '表彰加分',
    total_score      DECIMAL(5, 2)             COMMENT '综合得分',
    result           VARCHAR(10)               COMMENT 'excellent优秀 good良好 pass合格 fail不合格',
    assessor_id      BIGINT UNSIGNED           COMMENT '考核人',
    assess_date      DATE                      COMMENT '考核日期',
    comment          TEXT                      COMMENT '考核评语',
    created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_officer_id (officer_id),
    KEY idx_period (period_type, period_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='警员考核表';

CREATE TABLE equipment_info (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    equip_no        VARCHAR(50)      NOT NULL COMMENT '装备编号',
    equip_name      VARCHAR(100)     NOT NULL COMMENT '装备名称',
    equip_type      VARCHAR(50)      NOT NULL COMMENT '装备类型（字典值）',
    brand           VARCHAR(50),
    model           VARCHAR(100),
    purchase_date   DATE                      COMMENT '购置日期',
    useful_life     INT                       COMMENT '使用年限',
    purchase_price  DECIMAL(10, 2)            COMMENT '购置价格',
    dept_id         BIGINT UNSIGNED           COMMENT '归属部门',
    storage_location VARCHAR(200)             COMMENT '存放位置',
    status          VARCHAR(20)      NOT NULL DEFAULT 'idle' COMMENT 'idle空闲 borrowed借出 maintenance维修 scrapped报废',
    description     TEXT,
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    is_deleted      TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_equip_no (equip_no),
    KEY idx_equip_type (equip_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备档案表';

CREATE TABLE equipment_borrow (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    equipment_id    BIGINT UNSIGNED  NOT NULL COMMENT '装备ID',
    borrower_id     BIGINT UNSIGNED  NOT NULL COMMENT '借用人ID',
    borrow_purpose  VARCHAR(300)     NOT NULL COMMENT '借用用途',
    borrow_time     DATETIME         NOT NULL COMMENT '借出时间',
    expected_return DATETIME                  COMMENT '预计归还时间',
    actual_return   DATETIME                  COMMENT '实际归还时间',
    status          TINYINT(1)       NOT NULL DEFAULT 1 COMMENT '1借出中 2已归还',
    return_note     VARCHAR(300)              COMMENT '归还备注（损坏情况等）',
    approver_id     BIGINT UNSIGNED           COMMENT '审批人',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_equipment_id (equipment_id),
    KEY idx_borrower_id (borrower_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备借还记录表';

CREATE TABLE stat_case_daily (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    stat_date       DATE             NOT NULL COMMENT '统计日期',
    dept_id         BIGINT UNSIGNED           COMMENT '部门ID（NULL为全局）',
    total_cases     INT              NOT NULL DEFAULT 0 COMMENT '累计案件总数',
    new_cases       INT              NOT NULL DEFAULT 0 COMMENT '新增案件数',
    closed_cases    INT              NOT NULL DEFAULT 0 COMMENT '结案数',
    criminal_cases  INT              NOT NULL DEFAULT 0 COMMENT '刑事案件数',
    public_cases    INT              NOT NULL DEFAULT 0 COMMENT '治安案件数',
    traffic_cases   INT              NOT NULL DEFAULT 0 COMMENT '交通案件数',
    solve_rate      DECIMAL(5, 2)             COMMENT '破案率（%）',
    avg_solve_days  DECIMAL(8, 2)             COMMENT '平均侦查天数',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_date_dept (stat_date, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件每日统计表';

CREATE TABLE stat_alarm_daily (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    stat_date       DATE             NOT NULL,
    dept_id         BIGINT UNSIGNED,
    total_alarms    INT              NOT NULL DEFAULT 0 COMMENT '接警总数',
    disposed_alarms INT              NOT NULL DEFAULT 0 COMMENT '已处置数',
    dispose_rate    DECIMAL(5, 2)             COMMENT '处置率',
    avg_response_min DECIMAL(8, 2)            COMMENT '平均响应时长（分钟）',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_date_dept (stat_date, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='警情每日统计表';

-- =====================
-- 初始化角色数据
-- =====================
INSERT INTO sys_role (role_code, role_name, description, sort_order) VALUES
('ROLE_SUPER_ADMIN', '超级管理员', '系统最高权限，不受权限限制', 1),
('ROLE_DIRECTOR',    '局长/所长',  '全局数据查阅和报表权限',     2),
('ROLE_OFFICER',     '民警',       '日常业务操作权限',           3),
('ROLE_AUXILIARY',   '辅警',       '受限的辅助录入权限',         4),
('ROLE_DUTY',        '值班员',     '报警受理和任务派发权限',      5);

-- =====================
-- 初始化超级管理员账号
-- 密码：Admin@123456（BCrypt加密）
-- =====================
INSERT INTO sys_user (badge_no, username, password, real_name, gender, status) VALUES
('ADMIN000', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFkNkF3rlFwY6gRMRPm8yCO', '系统管理员', 1, 1);

-- =====================
-- 初始化数据字典
-- =====================
INSERT INTO sys_dict (dict_type, dict_label, dict_value, sort_order) VALUES
-- 案件类型
('case_category', '刑事案件', 'criminal', 1),
('case_category', '治安案件', 'public',   2),
('case_category', '交通案件', 'traffic',  3),
('case_category', '行政案件', 'admin',    4),
-- 案件小类
('case_type', '盗窃罪',   'theft',       1),
('case_type', '诈骗罪',   'fraud',       2),
('case_type', '抢劫罪',   'robbery',     3),
('case_type', '故意伤害', 'injury',      4),
('case_type', '交通肇事', 'traffic_acc', 5),
('case_type', '寻衅滋事', 'disturb',     6),
-- 案件状态
('case_status', '待立案',   'pending',      1),
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
('alarm_type', '盗窃警情',   'theft',    1),
('alarm_type', '斗殴警情',   'fight',    2),
('alarm_type', '诈骗警情',   'fraud',    3),
('alarm_type', '交通事故',   'traffic',  4),
('alarm_type', '火灾警情',   'fire',     5),
('alarm_type', '其他警情',   'other',    99),
-- 装备类型
('equipment_type', '枪支弹药', 'weapon',      1),
('equipment_type', '警用车辆', 'vehicle',     2),
('equipment_type', '通信设备', 'comm',        3),
('equipment_type', '防护装备', 'protection',  4),
('equipment_type', '办公设备', 'office',      5);

-- =====================
-- 初始化根部门
-- =====================
INSERT INTO sys_dept (parent_id, dept_name, dept_code, sort_order) VALUES
(0, '某市公安局', 'PSJ000', 1);

-- =====================
-- 初始化权限数据
-- perm_type: 1=菜单 2=操作按钮
-- =====================
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, path, sort_order, status) VALUES
-- 一级菜单
(0, 1, 'dashboard',      '警情驾驶舱',   '/dashboard',     1,  1),
(0, 1, 'alarm',          '报警受理',     '/alarm',         2,  1),
(0, 1, 'case',           '案件管理',     '/case',          3,  1),
(0, 1, 'person',         '人员档案',     '/person',        4,  1),
(0, 1, 'vehicle',        '车辆管理',     '/vehicle',       5,  1),
(0, 1, 'patrol',         '巡逻调度',     '/patrol',        6,  1),
(0, 1, 'officer',        '警力资源',     '/officer',       7,  1),
(0, 1, 'equipment',      '装备管理',     '/equipment',     8,  1),
(0, 1, 'stat',           '统计分析',     '/stat',          9,  1),
(0, 1, 'system',         '系统管理',     '/system',        10, 1);

-- 报警受理操作权限
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'alarm:view',     '报警查看',  1, 1 FROM sys_permission WHERE perm_code = 'alarm' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'alarm:add',      '接警录入',  2, 1 FROM sys_permission WHERE perm_code = 'alarm' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'alarm:dispatch', '任务派发',  3, 1 FROM sys_permission WHERE perm_code = 'alarm' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'alarm:close',    '警情关闭',  4, 1 FROM sys_permission WHERE perm_code = 'alarm' LIMIT 1;

-- 案件管理操作权限
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'case:view',  '案件查看',  1, 1 FROM sys_permission WHERE perm_code = 'case' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'case:add',   '案件立案',  2, 1 FROM sys_permission WHERE perm_code = 'case' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'case:edit',  '案件编辑',  3, 1 FROM sys_permission WHERE perm_code = 'case' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'case:del',   '案件删除',  4, 1 FROM sys_permission WHERE perm_code = 'case' LIMIT 1;

-- 人员档案操作权限
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'person:view', '人员查看', 1, 1 FROM sys_permission WHERE perm_code = 'person' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'person:add',  '人员新增', 2, 1 FROM sys_permission WHERE perm_code = 'person' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'person:edit', '人员编辑', 3, 1 FROM sys_permission WHERE perm_code = 'person' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'person:del',  '人员删除', 4, 1 FROM sys_permission WHERE perm_code = 'person' LIMIT 1;

-- 车辆管理操作权限
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'vehicle:view',    '车辆查看', 1, 1 FROM sys_permission WHERE perm_code = 'vehicle' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'vehicle:add',     '车辆登记', 2, 1 FROM sys_permission WHERE perm_code = 'vehicle' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'vehicle:edit',    '车辆编辑', 3, 1 FROM sys_permission WHERE perm_code = 'vehicle' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'vehicle:del',     '车辆删除', 4, 1 FROM sys_permission WHERE perm_code = 'vehicle' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'vehicle:control', '车辆布控', 5, 1 FROM sys_permission WHERE perm_code = 'vehicle' LIMIT 1;

-- 巡逻/警力操作权限
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'patrol:view',    '巡逻查看', 1, 1 FROM sys_permission WHERE perm_code = 'patrol' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'patrol:manage',  '巡逻管理', 2, 1 FROM sys_permission WHERE perm_code = 'patrol' LIMIT 1;

-- 系统管理操作权限
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:user:view',   '用户查看', 1, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:user:add',    '用户新增', 2, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:user:edit',   '用户编辑', 3, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:user:delete', '用户删除', 4, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:role:view',   '角色查看', 5, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:role:add',    '角色新增', 6, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:role:edit',   '角色编辑', 7, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:role:del',    '角色删除', 8, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:dept:view',   '部门查看', 9, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:dept:add',    '部门新增', 10, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:dept:edit',   '部门编辑', 11, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:dept:del',    '部门删除', 12, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;
INSERT INTO sys_permission (parent_id, perm_type, perm_code, perm_name, sort_order, status)
SELECT id, 2, 'sys:log:view',    '日志查看', 13, 1 FROM sys_permission WHERE perm_code = 'system' LIMIT 1;

-- =====================
-- 给超级管理员角色分配所有权限
-- =====================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'ROLE_SUPER_ADMIN'
  AND r.is_deleted = 0
  AND p.status = 1
  AND p.is_deleted = 0;


SET FOREIGN_KEY_CHECKS = 1;

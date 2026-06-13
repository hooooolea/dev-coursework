-- =====================================================
-- 智能警务管理系统 建表脚本
-- MySQL 8.0+
-- =====================================================

CREATE DATABASE IF NOT EXISTS smart_police
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_police;

SET FOREIGN_KEY_CHECKS = 0;

-- ===================== 系统管理 =====================

CREATE TABLE IF NOT EXISTS sys_dept (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT UNSIGNED  NOT NULL DEFAULT 0,
    dept_name   VARCHAR(100)     NOT NULL,
    dept_code   VARCHAR(50),
    leader_id   BIGINT UNSIGNED,
    phone       VARCHAR(20),
    address     VARCHAR(200),
    sort_order  INT              NOT NULL DEFAULT 0,
    status      TINYINT(1)       NOT NULL DEFAULT 1,
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    badge_no        VARCHAR(20)      NOT NULL,
    username        VARCHAR(50)      NOT NULL,
    password        VARCHAR(255)     NOT NULL,
    real_name       VARCHAR(50)      NOT NULL,
    gender          TINYINT(1)       NOT NULL DEFAULT 1,
    phone           VARCHAR(20),
    email           VARCHAR(100),
    dept_id         BIGINT UNSIGNED,
    avatar          VARCHAR(500),
    status          TINYINT(1)       NOT NULL DEFAULT 1,
    login_fail_count INT             NOT NULL DEFAULT 0,
    lock_until      DATETIME,
    last_login_at   DATETIME,
    last_login_ip   VARCHAR(45),
    remark          VARCHAR(500),
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    is_deleted      TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_badge_no (badge_no),
    UNIQUE KEY uk_username (username),
    KEY idx_dept_id (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    role_code   VARCHAR(50)      NOT NULL,
    role_name   VARCHAR(50)      NOT NULL,
    description VARCHAR(200),
    sort_order  INT              NOT NULL DEFAULT 0,
    status      TINYINT(1)       NOT NULL DEFAULT 1,
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  BIGINT UNSIGNED,
    is_deleted  TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    role_id    BIGINT UNSIGNED NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT UNSIGNED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_permission (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT UNSIGNED  NOT NULL DEFAULT 0,
    perm_type   TINYINT(1)       NOT NULL COMMENT '1菜单 2按钮/接口',
    perm_code   VARCHAR(100)     NOT NULL,
    perm_name   VARCHAR(100)     NOT NULL,
    icon        VARCHAR(100),
    path        VARCHAR(200),
    component   VARCHAR(200),
    sort_order  INT              NOT NULL DEFAULT 0,
    status      TINYINT(1)       NOT NULL DEFAULT 1,
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    role_id       BIGINT UNSIGNED NOT NULL,
    permission_id BIGINT UNSIGNED NOT NULL,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS sys_dict (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    dict_type   VARCHAR(50)      NOT NULL,
    dict_label  VARCHAR(100)     NOT NULL,
    dict_value  VARCHAR(100)     NOT NULL,
    sort_order  INT              NOT NULL DEFAULT 0,
    status      TINYINT(1)       NOT NULL DEFAULT 1,
    remark      VARCHAR(300),
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    user_id       BIGINT UNSIGNED,
    user_name     VARCHAR(50),
    module        VARCHAR(100),
    action        VARCHAR(100),
    method        VARCHAR(200),
    request_url   VARCHAR(500),
    request_ip    VARCHAR(45),
    request_body  TEXT,
    response_code INT,
    execute_time  INT,
    created_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at),
    KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ===================== 报警受理 =====================

CREATE TABLE IF NOT EXISTS alarm_record (
    id                 BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    alarm_no           VARCHAR(30)      NOT NULL,
    alarm_time         DATETIME         NOT NULL,
    caller_name        VARCHAR(50),
    caller_phone       VARCHAR(20),
    location_province  VARCHAR(20),
    location_city      VARCHAR(20),
    location_district  VARCHAR(20),
    location_detail    VARCHAR(200),
    alarm_type         VARCHAR(50)      NOT NULL,
    alarm_desc         TEXT,
    urgency_level      TINYINT(1)       NOT NULL DEFAULT 2,
    status             TINYINT(1)       NOT NULL DEFAULT 1,
    duty_user_id       BIGINT UNSIGNED,
    related_case_id    BIGINT UNSIGNED,
    close_time         DATETIME,
    close_summary      TEXT,
    attachment_url     VARCHAR(500),
    created_at         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by         BIGINT UNSIGNED,
    is_deleted         TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_alarm_no (alarm_no),
    KEY idx_alarm_time (alarm_time),
    KEY idx_status (status),
    KEY idx_urgency (urgency_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警记录表';

CREATE TABLE IF NOT EXISTS alarm_dispatch (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    alarm_id         BIGINT UNSIGNED  NOT NULL,
    officer_id       BIGINT UNSIGNED  NOT NULL,
    dispatch_time    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_arrive  DATETIME,
    arrive_time      DATETIME,
    status           TINYINT(1)       NOT NULL DEFAULT 1,
    result_desc      TEXT,
    complete_time    DATETIME,
    created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by       BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_alarm_id (alarm_id),
    KEY idx_officer_id (officer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='警情派发表';

-- ===================== 案件管理 =====================

CREATE TABLE IF NOT EXISTS case_info (
    id                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    case_no           VARCHAR(30)      NOT NULL,
    case_name         VARCHAR(200)     NOT NULL,
    case_category     VARCHAR(50)      NOT NULL,
    case_type         VARCHAR(50)      NOT NULL,
    occurred_at       DATETIME         NOT NULL,
    location_province VARCHAR(20),
    location_city     VARCHAR(20),
    location_district VARCHAR(20),
    location_detail   VARCHAR(200),
    case_desc         TEXT             NOT NULL,
    status            VARCHAR(20)      NOT NULL DEFAULT 'investigating',
    severity_level    TINYINT(1)       NOT NULL DEFAULT 1,
    lead_officer_id   BIGINT UNSIGNED,
    dept_id           BIGINT UNSIGNED,
    file_date         DATE,
    closed_date       DATE,
    cancelled_reason  VARCHAR(500),
    deadline_date     DATE,
    is_overdue        TINYINT(1)       NOT NULL DEFAULT 0,
    related_alarm_id  BIGINT UNSIGNED,
    remark            VARCHAR(500),
    created_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        BIGINT UNSIGNED,
    is_deleted        TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_no (case_no),
    KEY idx_status (status),
    KEY idx_lead_officer (lead_officer_id),
    KEY idx_file_date (file_date),
    KEY idx_case_type (case_type),
    KEY idx_severity (severity_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件信息表';

CREATE TABLE IF NOT EXISTS case_progress (
    id             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    case_id        BIGINT UNSIGNED  NOT NULL,
    progress_time  DATETIME         NOT NULL,
    content        TEXT             NOT NULL,
    next_plan      TEXT,
    officer_id     BIGINT UNSIGNED,
    created_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by     BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_case_id (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件侦查进展表';

CREATE TABLE IF NOT EXISTS case_evidence (
    id                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    case_id           BIGINT UNSIGNED  NOT NULL,
    evidence_no       VARCHAR(50)      NOT NULL,
    evidence_name     VARCHAR(200)     NOT NULL,
    evidence_type     VARCHAR(50)      NOT NULL,
    collect_time      DATETIME,
    collect_location  VARCHAR(200),
    collector_id      BIGINT UNSIGNED,
    storage_location  VARCHAR(200),
    description       TEXT,
    file_url          VARCHAR(500),
    file_type         VARCHAR(20),
    file_size         BIGINT,
    status            TINYINT(1)       NOT NULL DEFAULT 1,
    created_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        BIGINT UNSIGNED,
    is_deleted        TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_evidence_no (evidence_no),
    KEY idx_case_id (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件证据表';

CREATE TABLE IF NOT EXISTS case_suspect (
    id                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    case_id           BIGINT UNSIGNED  NOT NULL,
    person_id         BIGINT UNSIGNED,
    name              VARCHAR(50)      NOT NULL,
    id_card           VARCHAR(100),
    gender            TINYINT(1),
    phone             VARCHAR(20),
    address           VARCHAR(200),
    suspect_level     TINYINT(1)       NOT NULL DEFAULT 1,
    interrogation_summary TEXT,
    arrest_status     TINYINT(1)       NOT NULL DEFAULT 0,
    created_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_case_id (case_id),
    KEY idx_person_id (person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件嫌疑人表';

-- ===================== 人员档案 =====================

CREATE TABLE IF NOT EXISTS person_info (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    name             VARCHAR(50)      NOT NULL,
    gender           TINYINT(1)       NOT NULL,
    ethnicity        VARCHAR(20),
    id_card          VARCHAR(255)     NOT NULL,
    id_card_tail     VARCHAR(4),
    birth_date       DATE,
    phone            VARCHAR(255),
    phone_tail       VARCHAR(4),
    household_addr   VARCHAR(300),
    current_addr     VARCHAR(300),
    occupation       VARCHAR(100),
    work_unit        VARCHAR(200),
    photo_url        VARCHAR(500),
    person_type      VARCHAR(20)      NOT NULL DEFAULT 'normal',
    type_reason      VARCHAR(500),
    type_operator_id BIGINT UNSIGNED,
    type_at          DATETIME,
    remark           TEXT,
    dept_id          BIGINT UNSIGNED,
    created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by       BIGINT UNSIGNED,
    is_deleted       TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_id_card (id_card),
    KEY idx_name (name),
    KEY idx_person_type (person_type),
    KEY idx_id_card_tail (id_card_tail)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员档案表';

CREATE TABLE IF NOT EXISTS person_violation (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    person_id       BIGINT UNSIGNED  NOT NULL,
    violation_time  DATETIME         NOT NULL,
    violation_type  VARCHAR(50)      NOT NULL,
    description     TEXT,
    result          VARCHAR(50),
    fine_amount     DECIMAL(10,2),
    detention_days  INT,
    related_case_id BIGINT UNSIGNED,
    handler_id      BIGINT UNSIGNED,
    dept_id         BIGINT UNSIGNED,
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_person_id (person_id),
    KEY idx_violation_time (violation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员违法记录表';

-- ===================== 车辆管理 =====================

CREATE TABLE IF NOT EXISTS vehicle_info (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    plate_no        VARCHAR(20)      NOT NULL,
    vin             VARCHAR(50),
    vehicle_type    VARCHAR(20)      NOT NULL,
    brand           VARCHAR(50),
    model           VARCHAR(100),
    color           VARCHAR(20),
    year            INT,
    owner_person_id BIGINT UNSIGNED,
    owner_name      VARCHAR(50),
    register_date   DATE,
    expire_date     DATE,
    photo_url       VARCHAR(500),
    status          VARCHAR(20)      NOT NULL DEFAULT 'normal',
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

CREATE TABLE IF NOT EXISTS vehicle_violation (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    vehicle_id       BIGINT UNSIGNED  NOT NULL,
    violation_time   DATETIME         NOT NULL,
    violation_type   VARCHAR(50)      NOT NULL,
    location         VARCHAR(200),
    fine_amount      DECIMAL(10,2),
    deducted_points  INT              NOT NULL DEFAULT 0,
    is_paid          TINYINT(1)       NOT NULL DEFAULT 0,
    paid_at          DATETIME,
    related_case_id  BIGINT UNSIGNED,
    evidence_url     VARCHAR(500),
    handler_id       BIGINT UNSIGNED,
    created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_violation_time (violation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆违章表';

CREATE TABLE IF NOT EXISTS vehicle_control (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    vehicle_id       BIGINT UNSIGNED  NOT NULL,
    control_reason   VARCHAR(500)     NOT NULL,
    control_level    TINYINT(1)       NOT NULL DEFAULT 1,
    start_time       DATETIME         NOT NULL,
    end_time         DATETIME,
    related_case_id  BIGINT UNSIGNED,
    status           TINYINT(1)       NOT NULL DEFAULT 1,
    decontrol_time   DATETIME,
    decontrol_reason VARCHAR(300),
    operator_id      BIGINT UNSIGNED,
    created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by       BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆布控表';

-- ===================== 巡逻调度 =====================

CREATE TABLE IF NOT EXISTS patrol_schedule (
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    officer_id    BIGINT UNSIGNED  NOT NULL,
    schedule_date DATE             NOT NULL,
    shift_type    VARCHAR(10)      NOT NULL,
    start_time    TIME             NOT NULL,
    end_time      TIME             NOT NULL,
    dept_id       BIGINT UNSIGNED,
    status        VARCHAR(10)      NOT NULL DEFAULT 'normal',
    remark        VARCHAR(200),
    created_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by    BIGINT UNSIGNED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_officer_date_shift (officer_id, schedule_date, shift_type),
    KEY idx_schedule_date (schedule_date),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡逻排班表';

CREATE TABLE IF NOT EXISTS patrol_task (
    id             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    task_no        VARCHAR(30)      NOT NULL,
    task_type      VARCHAR(20)      NOT NULL,
    task_name      VARCHAR(200)     NOT NULL,
    officer_id     BIGINT UNSIGNED  NOT NULL,
    area_name      VARCHAR(100),
    task_start     DATETIME         NOT NULL,
    task_end       DATETIME         NOT NULL,
    status         VARCHAR(20)      NOT NULL DEFAULT 'pending',
    accept_time    DATETIME,
    complete_time  DATETIME,
    summary        TEXT,
    dispatch_by    BIGINT UNSIGNED,
    created_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by     BIGINT UNSIGNED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_officer_id (officer_id),
    KEY idx_status (status),
    KEY idx_task_start (task_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡逻任务表';

CREATE TABLE IF NOT EXISTS patrol_checkin (
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    task_id       BIGINT UNSIGNED  NOT NULL,
    officer_id    BIGINT UNSIGNED  NOT NULL,
    checkin_type  VARCHAR(10)      NOT NULL COMMENT 'start/end/mid',
    checkin_time  DATETIME         NOT NULL,
    longitude     DECIMAL(10,7),
    latitude      DECIMAL(10,7),
    location_desc VARCHAR(200),
    note          VARCHAR(500),
    created_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_officer_id (officer_id),
    KEY idx_checkin_time (checkin_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡逻打卡签到表';

-- ===================== 警力资源 =====================

CREATE TABLE IF NOT EXISTS officer_info (
    id                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    user_id           BIGINT UNSIGNED  NOT NULL,
    badge_no          VARCHAR(20)      NOT NULL,
    real_name         VARCHAR(50)      NOT NULL,
    gender            TINYINT(1)       NOT NULL,
    police_category   VARCHAR(20),
    rank_title        VARCHAR(30),
    position          VARCHAR(50),
    dept_id           BIGINT UNSIGNED,
    entry_date        DATE,
    id_card           VARCHAR(255),
    phone             VARCHAR(255),
    emergency_contact VARCHAR(50),
    emergency_phone   VARCHAR(255),
    skills            VARCHAR(500),
    work_status       VARCHAR(20)      NOT NULL DEFAULT 'on_duty',
    photo_url         VARCHAR(500),
    remark            TEXT,
    created_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        BIGINT UNSIGNED,
    is_deleted        TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_badge_no (badge_no),
    KEY idx_dept_id (dept_id),
    KEY idx_work_status (work_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='警员档案表';

CREATE TABLE IF NOT EXISTS equipment_info (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    equip_no         VARCHAR(50)      NOT NULL,
    equip_name       VARCHAR(100)     NOT NULL,
    equip_type       VARCHAR(50)      NOT NULL,
    brand            VARCHAR(50),
    model            VARCHAR(100),
    purchase_date    DATE,
    useful_life      INT,
    purchase_price   DECIMAL(10,2),
    dept_id          BIGINT UNSIGNED,
    storage_location VARCHAR(200),
    status           VARCHAR(20)      NOT NULL DEFAULT 'idle',
    description      TEXT,
    created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by       BIGINT UNSIGNED,
    is_deleted       TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_equip_no (equip_no),
    KEY idx_equip_type (equip_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备档案表';

CREATE TABLE IF NOT EXISTS equipment_borrow (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    equipment_id     BIGINT UNSIGNED  NOT NULL,
    borrower_id      BIGINT UNSIGNED  NOT NULL,
    borrow_purpose   VARCHAR(300)     NOT NULL,
    borrow_time      DATETIME         NOT NULL,
    expected_return  DATETIME,
    actual_return    DATETIME,
    status           TINYINT(1)       NOT NULL DEFAULT 1,
    return_note      VARCHAR(300),
    approver_id      BIGINT UNSIGNED,
    created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by       BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_equipment_id (equipment_id),
    KEY idx_borrower_id (borrower_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备借还记录表';

SET FOREIGN_KEY_CHECKS = 1;

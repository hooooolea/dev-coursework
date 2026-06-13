# 智能警务管理系统

## 技术栈

- **后端**: Java 17 · Spring Boot 3.2 · Spring Security · MyBatis-Plus
- **前端**: Vue 3 · Element Plus · ECharts · Vite
- **数据库**: MySQL 8.0 · Redis 7
- **工具**: Lombok · Hutool · Apache POI · JWT · Quartz

## 项目结构

```
smart-police/          # Spring Boot 后端
smart-police-web/      # Vue 3 前端
smart_police.sql       # 数据库完整 DDL（30 张表）
```

## 快速启动

### 1. 数据库

```bash
# 首次初始化
mysql -u root -p < smart_police.sql

# 或分步执行
mysql -u root -p < sql/01_create_tables.sql
mysql -u root -p smart_police < sql/02_init_data.sql
```

### 2. 修改配置

编辑 `src/main/resources/application-dev.yml`，确认 MySQL 密码和 Redis 配置。

### 3. 启动后端

```bash
cd smart-police
mvn spring-boot:run -DskipTests
# 端口: 8081
```

### 4. 启动前端

```bash
cd smart-police-web
npm install
npm run dev
# 端口: 5173
```

## 登录

- 地址: http://localhost:5173
- 账号: `admin`
- 密码: `123456`

## 接口列表

| 模块 | 接口 | 说明 |
|------|------|------|
| 认证 | POST /api/auth/login | 登录 |
| 认证 | POST /api/auth/logout | 退出 |
| 用户 | GET /api/user/list | 用户列表 |
| 用户 | POST /api/user | 新增用户 |
| 字典 | GET /api/dict/{type} | 按类型查字典 |
| 报警 | POST /api/alarm | 接警录入 |
| 报警 | PUT /api/alarm/{id}/dispatch | 任务派发 |
| 报警 | PUT /api/alarm/{id}/close | 关闭警情 |
| 案件 | GET /api/case/list | 案件列表 |
| 案件 | POST /api/case | 案件立案 |
| 案件 | PUT /api/case/{id}/status | 变更状态 |
| 案件 | POST /api/case/{id}/progress | 新增进展 |
| 人员 | GET /api/person/list | 人员列表 |
| 人员 | POST /api/person | 新增人员 |
| 车辆 | GET /api/vehicle/list | 车辆列表 |
| 车辆 | POST /api/vehicle | 登记车辆 |
| 车辆 | POST /api/vehicle/{id}/control | 设置布控 |
| 车辆 | PUT /api/vehicle/{id}/decontrol | 解除布控 |
| 巡逻 | GET /api/patrol/task/list | 巡逻任务列表 |
| 巡逻 | POST /api/patrol/task | 新建巡逻任务 |
| 巡逻 | PUT /api/patrol/task/{id}/accept | 接收任务 |
| 巡逻 | POST /api/patrol/task/{id}/checkin | 打卡签到 |

## 模块说明

- **报警/警情**: 接警 → 派发 → 处警全流程
- **案件**: 立案 → 侦查进展 → 证据 → 涉案人员
- **人员档案**: 重点人员标签化管理
- **车辆**: 车辆登记 +，布控报警
- **巡逻调度**: 排班 + 任务派发 + 打卡
- **装备管理**: 装备档案 + 借用归还
- **统计分析**: 案件/警情日报统计
- **系统管理**: 用户/角色/部门/字典/操作日志

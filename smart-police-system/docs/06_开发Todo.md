# 智能警务管理系统 — 开发 Todo

> 更新时间：2026-06-10
> 规则：先完成主体业务功能，再推进 AI 模块。

---

## ✅ 已完成

### 后端基础设施
- [x] Spring Boot 3 + Spring Security + JWT 认证
- [x] MyBatis-Plus + 分页插件
- [x] 统一响应 `Result<T>` + 全局异常处理
- [x] AOP 操作日志 `@OperationLog`
- [x] 数据脱敏工具 `DesensitizeUtil`
- [x] 建表 SQL `smart_police.sql`（30 张表）

### 系统管理模块
- [x] 用户管理（CRUD + 角色分配 + 重置密码）
- [x] 角色管理（CRUD + 权限树分配）
- [x] 部门管理（树形 CRUD）
- [x] 数据字典管理
- [x] 操作日志查询页

### 业务模块
- [x] 报警受理（接警/派发/到达/关闭）
- [x] 案件管理（立案/编辑/侦查进展/证据管理/状态流转）
- [x] 人员档案（CRUD + 标注 + 违法记录）
- [x] 车辆管理（CRUD + 布控/解控 + 违章记录）
- [x] 巡逻调度（任务 CRUD + 签到打卡 + 完成提交）
- [x] 排班管理（周视图 + 新增/删除排班）
- [x] 警力资源（警员 CRUD + 状态更新 + 绩效考核）
- [x] 装备物资（档案 CRUD + 领用/归还）
- [x] 统计分析（驾驶舱 + 案件统计 + 警力效能）
- [x] Excel 报表导出（警情/案件/警员）

### 前端
- [x] 登录页 / Layout 布局（侧边菜单 + 顶部导航）
- [x] 全部业务模块对应 View
- [x] Dashboard 对接真实统计数据
- [x] 所有模块删除功能（二次确认）

---

## 🔲 待完成：主体功能（优先做）

### P0 — 影响正常使用

- [ ] **文件上传基础设施**
  - 后端：本地存储 / MinIO 接口（`/api/upload`），返回文件 URL
  - 案件证据：fileUrl 目前是文本输入，改为真实上传
  - 人员档案：photoUrl 补充头像上传
  - 考虑：先用本地存储，路径 `/uploads/{year}/{month}/{uuid}.{ext}`

- [ ] **案件嫌疑人管理**
  - 设计文档有，代码未实现
  - 后端：`case_suspect` 表（已在 SQL）→ Mapper + 接口（添加/删除/标注嫌疑程度）
  - 前端：CaseView 嫌疑人抽屉（关联人员档案搜索 + 列表）

- [ ] **PatrolView 与 PatrolTask 字段对齐验证**
  - 实体已修复（taskName / areaName / summary），需确认前端 form 提交字段名是否与后端一致
  - 测试：新增任务 → 列表能正常显示任务名称和区域

### P1 — 完善体验

- [ ] **操作日志 Excel 导出**
  - 设计文档 1.5 节提到"支持导出 Excel"，目前日志页只有列表
  - 在 OperationLogController 加 `/api/operation-log/export` 接口
  - 前端 LogView 加导出按钮

- [ ] **角色管理 — 权限树展示优化**
  - 目前权限表（sys_permission）可能没有初始化数据
  - 需要在 SQL 或接口中补充初始权限编码数据（case:view / alarm:view 等）
  - 确保权限树能正常展示、勾选、保存

- [ ] **用户管理 — 分配角色功能**
  - UserView 目前有用户 CRUD，但没有「分配角色」按钮
  - 需要新增角色分配弹窗（多选复选框）+ 后端 `/api/user/{id}/roles` 接口

- [ ] **Dashboard — 统计卡片 loading 状态**
  - 首次加载时卡片显示 `-`，加载失败没有提示
  - 补充 error 状态展示和 retry 按钮

- [ ] **巡逻 PatrolView — 派发任务警员选择**
  - 目前是远程搜索下拉，但如果 officer_info 表为空，用户不知道如何测试
  - 确认 officerApi.list 接口能正常返回在岗警员

### P2 — 锦上添花

- [ ] **全局 404 / 403 页面**
  - router 目前只有 `/:pathMatch(.*)* → /`，无提示
  - 加 404.vue 和权限不足提示页

- [ ] **表格空状态统一**
  - 各 View 数据为空时显示 el-empty，目前部分页面空表格无提示

- [ ] **密码强度校验**
  - UserView 重置密码时加前端校验（≥8位，含数字和字母）

- [ ] **侧边菜单 — 系统管理子菜单折叠**
  - 目前系统管理 4 个子菜单直接展开，条目较多
  - 可改为 el-sub-menu 折叠分组

- [ ] **统计分析 — 同比/环比**
  - StatView 目前只有绝对数量，可加本月 vs 上月的变化幅度

---

## 🤖 AI 模块（主体完成后推进）

> 详见 `05_AI智能板块设计.md`，下方为精简 Todo 清单。

### 阶段一：基础设施

- [ ] `MiMoConfig.java` — 配置读取（API Key 从环境变量）
- [ ] `MiMoClient.java` — OkHttp 封装（SSE 流式 + 普通 JSON）
- [ ] `PromptBuilder.java` — 模板渲染
- [ ] `ai_conversation` 建表 + Mapper
- [ ] 连通性测试（调通 mimo-v2.5-pro）

### 阶段二：智能排班 + 装备推荐

- [ ] `AiScheduleService` + `AiController.scheduleRecommend`（SSE）
- [ ] 前端 PatrolView — AI 推荐警员按钮 + 流式展示 + 一键填入
- [ ] `AiEquipmentService` + 前端展示分级装备清单

### 阶段三：警情研判 + 自然语言查询

- [ ] `AiAnalysisService` + 统计分析页 AI Tab
- [ ] 全局悬浮 AI 助手组件（文字对话）

### 阶段四：多模态 — 图片/视频

- [ ] `AiOmniService` — 图片 AI 描述（案件证据上传后自动触发）
- [ ] （可选）巡逻视频摘要

### 阶段五：语音 ASR + TTS

- [ ] `AiAsrService` + 前端 AlarmView 语音接警模式
- [ ] 前端 PatrolView 签到语音上报
- [ ] `AiTtsService` + 紧急警情自动语音播报

---

## 🗂 文档

- [x] `01_系统架构设计.md`
- [x] `02_功能模块设计.md`
- [x] `03_数据库设计.md`
- [x] `04_项目结构与开发规范.md`
- [x] `05_AI智能板块设计.md`（MiMo V2.5，含 ASR/TTS/Omni 多模态规划）
- [x] `06_开发Todo.md`（本文件）

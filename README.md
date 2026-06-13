# 智能警务管理系统 (Smart Police Management System)

Java Spring Boot 3 + Vue 3 全栈项目，面向公安机关的数字化警务管理平台。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21, Spring Boot 3.2, Spring Security 6, MyBatis-Plus 3.5, JWT, Redis |
| 前端 | Vue 3, Element Plus, ECharts 5, Vite |
| 数据库 | MySQL 8.0 |
| AI | MiMo 大模型 (文本推理 + 多模态 + ASR + TTS) |

## 功能模块

- **警情驾驶舱** — 实时统计卡片 + 趋势图 + 案件类型分布
- **报警受理** — 接警录入 → 任务派发 → 到场处置 → 关闭
- **案件管理** — 立案/侦查进展/证据管理/嫌疑人管理/状态流转
- **人员档案** — 人口信息 + 分类标注 + 违法记录
- **车辆管理** — 车辆登记 + 布控/解控 + 违章记录
- **巡逻调度** — 周排班 + 任务派发 + 签到打卡
- **警力资源** — 警员档案 + 绩效考核 + 装备物资
- **统计分析** — 案件趋势 + 警力效能 + 同比/环比 + Excel 导出
- **系统管理** — 用户/角色/权限(RBAC) + 部门 + 字典 + 操作日志

## AI 智能辅助

| 功能 | 说明 |
|------|------|
| 智能排班推荐 | 根据任务/区域/警员数据，AI 推荐最优组合 (SSE 流式) |
| 装备推荐 | 根据任务类型和区域风险，分级推荐装备 |
| AI 警情研判 | 近30天数据汇总分析，趋势预测 + 风险识别 |
| 全局 AI 助手 | 页面右下角悬浮窗，自然语言查询数据 |
| 语音接警 | 上传录音 / 实时录音 → ASR 识别 → 自动填表 |
| 视频分析 | 上传巡逻视频 → AI 分析场景/识别可疑特征 |
| TTS 播报 | 紧急警情自动语音播报 |

## 快速启动

### 环境要求

- JDK 21, Maven 3.9+, MySQL 8.0, Redis 7, Node.js 18+

### 1. 初始化数据库

```bash
mysql -u root -p < smart_police.sql        # 建表
mysql -u root -p < sql/02_init_data.sql     # 初始数据（字典/权限）
mysql -u root -p < sql/04_ai_conversation.sql  # AI 对话表
mysql -u root -p < sql/05_demo_data.sql     # 演示数据
```

### 2. 配置数据库连接

编辑 `smart-police/src/main/resources/application-dev.yml`，修改数据库密码：

```yaml
spring:
  datasource:
    druid:
      username: root
      password: 你的密码
```

### 3. 启动后端

```bash
cd smart-police
mvn spring-boot:run
# → http://localhost:8081
```

### 4. 启动前端

```bash
cd smart-police-web
npm install
npm run dev
# → http://localhost:5174
```

### 5. 登录

默认账号：`admin` / `123456`

### 6. 配置 AI 功能

登录后进入「系统管理 → AI配置」，填入 MiMo API Key 即可使用全部 AI 功能。

## 项目结构

```
plice/
├── smart-police/          # Spring Boot 后端
│   ├── src/main/java/com/police/
│   │   ├── ai/            # AI 模块 (client/service/controller)
│   │   ├── system/        # 系统管理
│   │   ├── alarm/         # 报警受理
│   │   ├── caseinfo/      # 案件管理
│   │   ├── person/        # 人员档案
│   │   ├── vehicle/       # 车辆管理
│   │   ├── patrol/        # 巡逻调度
│   │   ├── officer/       # 警力资源
│   │   ├── stat/          # 统计分析
│   │   └── common/        # 公共组件
│   └── sql/               # 数据库脚本
├── smart-police-web/      # Vue 3 前端
│   └── src/
│       ├── views/         # 页面组件 (15个)
│       ├── api/           # 接口封装
│       ├── components/    # 公共组件
│       └── router/        # 路由配置
├── *.md                   # 设计文档 + 大作业报告
└── smart_police.sql       # 完整建表 SQL
```

## 权限体系

| 角色 | 权限范围 |
|------|---------|
| 超级管理员 | 全部 60+ 权限 |
| 局长/所长 | 全局只读 + 报表导出 |
| 民警 | 报警/案件/人员/车辆/巡逻 日常操作 |
| 辅警 | 报警录入 + 巡逻打卡 |
| 值班员 | 报警受理全权 |

## License

Educational project.

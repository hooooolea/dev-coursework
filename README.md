# Dev Coursework

开发课程作业合集。

## 项目列表

| 项目 | 技术栈 | 说明 |
|------|--------|------|
| [智能警务管理系统](./) | Spring Boot 3 + Vue 3 + MySQL + Redis + AI | Java Web 大作业 |

## 项目结构

```
dev-coursework/
├── backend/               # Spring Boot 后端 (121 个 Java 文件)
├── frontend/              # Vue 3 前端 (38 个 Vue/JS 文件)
├── smart_police.sql       # 数据库建表 SQL
├── 01-06_*.md             # 设计文档
├── 大作业报告.md           # 课程报告
└── README.md              # 本文件
```

## 快速启动

```bash
# 后端
cd backend && mvn spring-boot:run     # → http://localhost:8081

# 前端
cd frontend && npm install && npm run dev  # → http://localhost:5174

# 数据库
mysql -u root -p < smart_police.sql
```

默认账号：`admin` / `123456`

# Career Assistant

一个证据可追溯的 AI 求职与面试准备助手。项目目标是把简历、岗位描述和用户补充信息转化为可确认的事实、差距分析与行动建议，同时保留每条结论的来源。

新对话或协作者请先阅读 [`docs/PROJECT_CONTEXT.md`](docs/PROJECT_CONTEXT.md)，其中记录了产品决策、用户画像、当前仓库状态、开发顺序和不可夸大的能力边界。

## 第一阶段目标

- 录入简历文本与岗位描述。
- 提取教育、工作、项目和技能事实，并返回原文证据。
- 区分事实、推断和建议。
- 由用户确认或修正提取结果。
- 生成岗位匹配说明与面试准备清单。
- 支持同一公司多岗位比较、硬性条件筛选与前 N 个投递推荐。
- 建立可重复运行的评测集，记录证据覆盖率和无依据生成率。
- 为 Java→Python 调用链增加 Actuator、Prometheus、Grafana 和脱敏故障日志。

## 目录

```text
apps/web/                 前端应用，第二阶段使用 Vue 3
services/core-api/        Spring Boot 业务 API
services/ai-service/      FastAPI AI 服务
docs/                     PRD、架构和接口文档
evals/                    评测样例与指标
infra/                    Docker 与部署配置
```

## 当前可运行模块

### Spring Boot API

```powershell
cd services/core-api
mvn spring-boot:run
```

健康检查：`GET http://localhost:8080/api/health`

### FastAPI AI 服务

```powershell
cd services/ai-service
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

健康检查：`GET http://localhost:8001/health`

第一版事实提取接口使用本地规则模拟输出，不依赖付费模型或 API Key。后续再替换为可配置的 LLM Provider。

## 六周里程碑

1. Spring Boot CRUD 与数据模型。
2. 文档录入和来源段落切分。
3. FastAPI 事实提取与结构化输出。
4. 岗位匹配、人工确认和建议生成。
5. RAG、安全控制与评测集。
6. Docker、演示、README 与简历包装。

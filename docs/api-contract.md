# Initial API Contract

## Core API

- `GET /api/health`：健康检查。
- `POST /api/documents`：创建候选人文档。
- `POST /api/jobs`：创建岗位描述。
- `POST /api/analyses`：发起岗位匹配分析。
- `PATCH /api/facts/{id}/review`：确认、修改或拒绝事实。
- `GET /api/analyses/{id}`：读取分析结果。

## AI Service

- `GET /health`：健康检查。
- `POST /v1/facts/extract`：从文本中提取带来源的候选事实。
- `POST /v1/matches/analyse`：根据已确认事实分析岗位匹配。
- `POST /v1/evaluations/run`：运行离线评测。

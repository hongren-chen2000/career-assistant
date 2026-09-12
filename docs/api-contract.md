# Initial API Contract

## Core API

- `GET /api/health`：健康检查。
- `POST /api/documents`：创建候选人文档。
- `GET /api/documents/{id}`：按 ID 读取候选人文档；不存在时返回 `404`。
- `POST /api/jobs`：创建岗位描述。
- `POST /api/analyses`：发起岗位匹配分析。
- `PATCH /api/facts/{id}/review`：确认、修改或拒绝事实。
- `GET /api/analyses/{id}`：读取分析结果。
- `POST /api/job-comparisons`：比较同一公司的多个岗位，并按投递上限返回排序结果。
- `GET /api/job-comparisons/{id}`：读取资格判断、匹配证据、差距和推荐等级。
- `GET /actuator/health`：Spring Boot Actuator 健康状态。
- `GET /actuator/prometheus`：Prometheus 指标端点。

## AI Service

- `GET /health`：健康检查。
- `POST /v1/facts/extract`：从文本中提取带来源的候选事实。
- `POST /v1/matches/analyse`：根据已确认事实分析岗位匹配。
- `POST /v1/jobs/requirements/extract`：将岗位文本解析为硬性要求、核心要求和优先条件。
- `POST /v1/evaluations/run`：运行离线评测。

## 匹配结果最低字段

- `eligibility`：`eligible`、`ineligible` 或 `unknown`，以及逐项原因。
- `matched_requirements`：岗位要求、对应的已确认 `fact_id` 和证据引用。
- `missing_requirements`：暂无事实支持的要求，不得写成用户不会。
- `transferable_strengths`：可迁移但并非直接满足的经历。
- `risk_flags`：筛选或面试风险。
- `score_breakdown`：各维度分数和权重。
- `recommendation_level`：`recommended`、`stretch` 或 `not_recommended`。
- `next_actions`：投递前可真实完成的补强任务。

以上接口属于目标契约；只有代码和测试落地后才能标记为“已实现”。

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
- `POST /api/documents/{id}/facts/extract`：同步提取、校验并保存候选事实；已实现。
- `GET /api/documents/{id}/facts`：查询文档已保存的候选事实；已实现。

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

## 文档事实提取与查询（已实现）

### POST /api/documents/{id}/facts/extract

请求路径中的 id 为文档 UUID，无需请求体。

处理流程：
读取文档原文 → 调用 AI Service → 校验证据 → 整批保存候选事实。

- 成功：200，返回 FactResponse 数组。
- 文档不存在：404，不调用 AI Service。
- AI 调用失败或证据无效：502，返回安全的 ProblemDetail。
- 新事实的审核状态由 Core API 初始化为 PENDING_REVIEW。
- 整批校验成功后才更新存储；失败时保留原有事实。
- 当前再次成功提取会替换该文档原有的候选事实列表。
  加入人工审核功能前，需要完善重复提取规则。

### GET /api/documents/{id}/facts

- 文档存在：200，返回已保存的 FactResponse 数组。
- 文档存在但尚未提取：200，返回 []。
- 文档不存在：404。
- 查询不会调用 AI Service。

### FactResponse 字段

| 字段 | 类型 | 含义 |
|---|---|---|
| id | UUID | 候选事实 ID |
| documentId | UUID | 来源文档 ID |
| factType | string | EDUCATION、EXPERIENCE、PROJECT 或 SKILL |
| value | string | 候选事实内容 |
| evidence.sourceText | string | 原文证据片段 |
| evidence.start | integer | 起点，包含 |
| evidence.end | integer | 终点，不包含 |
| confidence | number | AI 置信度，不代表用户确认 |
| status | string | 新提取事实为 PENDING_REVIEW |
| createdAt | string | ISO-8601 创建时间 |

证据位置相对于未经 trim 或其他归一化处理的原始文档，
使用 Unicode 码点索引，区间为 [start, end)。
Java 校验时转换为 UTF-16 索引，保存时仍保留码点索引。

### 当前实现范围

- 文档和候选事实使用内存存储，服务重启后数据清空。
- FastAPI 使用确定性 mock 提取，尚未接入真实 LLM。
- 已完成真实 HTTP 联调。
- 人工确认、修改和拒绝接口尚未实现。
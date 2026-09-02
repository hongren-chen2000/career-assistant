# Architecture

```text
Vue 3 Web
    |
    v
Spring Boot Core API ---- PostgreSQL
    |                     Redis
    v
FastAPI AI Service ---- Configurable LLM Provider
    |
    v
Evidence extraction / matching / evaluation

Spring Boot Actuator/Micrometer ---> Prometheus ---> Grafana
```

## 设计原则

- Spring Boot 负责业务对象、权限、审核状态和持久化。
- FastAPI 负责文本处理、模型调用和评测。
- AI 输出只返回结构化候选结果，最终事实由用户确认。
- 模型供应商通过接口隔离，MVP 可在 mock 模式下运行。
- 日志不得记录完整简历、邮箱、电话等敏感信息。
- Core API 负责记录跨服务调用指标；FastAPI 不负责伪造 Java 侧业务指标。
- 岗位匹配先执行硬性条件检查，再进行带证据的维度评分和推荐排序。
- 监控日志只保留请求 ID、目标接口、状态、耗时和错误类型。

## 可观测性最小闭环

- Actuator 提供健康信息和 Prometheus 指标端点。
- Micrometer 统计 Java→Python 调用次数、结果和耗时分布。
- Prometheus 定时抓取 Core API。
- Grafana 展示调用量、成功率、P95 延迟和错误数。
- 故障演练通过停止 Python 服务验证错误处理、日志和指标，不记录请求正文。

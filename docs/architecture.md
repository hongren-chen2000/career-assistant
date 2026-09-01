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
```

## 设计原则

- Spring Boot 负责业务对象、权限、审核状态和持久化。
- FastAPI 负责文本处理、模型调用和评测。
- AI 输出只返回结构化候选结果，最终事实由用户确认。
- 模型供应商通过接口隔离，MVP 可在 mock 模式下运行。
- 日志不得记录完整简历、邮箱、电话等敏感信息。

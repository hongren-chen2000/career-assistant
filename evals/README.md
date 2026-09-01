# Evaluation Plan

第一阶段使用虚构或脱敏数据，避免将真实个人信息提交到外部模型。

## 指标

- Evidence coverage：事实中带有效来源的比例。
- Unsupported claim rate：无法由输入支持的结论比例。
- Fact precision：人工确认后为正确的事实比例。
- Review correction rate：需要人工修改的事实比例。
- Latency：端到端响应时间。

## 初始数据集

- 10 份虚构简历文本。
- 每份简历配 1-2 个岗位描述。
- 每组数据提供人工标注事实和预期缺口。

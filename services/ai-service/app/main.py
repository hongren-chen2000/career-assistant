from enum import Enum

from fastapi import FastAPI
from pydantic import BaseModel, Field


app = FastAPI(title="Career Assistant AI Service", version="0.1.0")


class FactType(str, Enum):
    EDUCATION = "education"
    EXPERIENCE = "experience"
    PROJECT = "project"
    SKILL = "skill"


class ExtractFactsRequest(BaseModel):
    text: str = Field(min_length=1, max_length=50_000)


class EvidenceSpan(BaseModel):
    source_text: str
    start: int
    end: int


class CandidateFact(BaseModel):
    fact_type: FactType
    value: str
    evidence: EvidenceSpan
    confidence: float = Field(ge=0, le=1)
    status: str = "pending_review"


class ExtractFactsResponse(BaseModel):
    mode: str
    facts: list[CandidateFact]


@app.get("/health")
def health() -> dict[str, str]:
    return {"service": "career-assistant-ai-service", "status": "ok"}


@app.post("/v1/facts/extract", response_model=ExtractFactsResponse)
def extract_facts(request: ExtractFactsRequest) -> ExtractFactsResponse:
    """A deterministic placeholder used before an external LLM is configured."""
    original_text = request.text

    first_line = next(
        (
            line.strip()
            for line in original_text.splitlines()
            if line.strip()
        ),
        original_text.strip(),
    )

    # EvidenceSpan 必须始终相对于调用方提交的原始文本计算。
    start = original_text.find(first_line)

    fact = CandidateFact(
        fact_type=FactType.EXPERIENCE,
        value=first_line,
        evidence=EvidenceSpan(
            source_text=first_line,
            start=start,
            end=start + len(first_line),
        ),
        confidence=0.50,
    )

    return ExtractFactsResponse(mode="mock", facts=[fact])

from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_health() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_extract_facts_keeps_source_span() -> None:
    response = client.post("/v1/facts/extract", json={"text": "Java backend experience\nPython course project"})
    assert response.status_code == 200
    fact = response.json()["facts"][0]
    assert fact["value"] == "Java backend experience"
    assert fact["evidence"]["start"] == 0
    assert fact["status"] == "pending_review"

def test_extract_facts_evidence_offsets_reference_original_text() -> None:
    original_text = "\n  Java backend experience\nPython course project"

    response = client.post(
        "/v1/facts/extract",
        json={"text": original_text},
    )

    assert response.status_code == 200

    fact = response.json()["facts"][0]
    evidence = fact["evidence"]

    expected_start = original_text.index("Java backend experience")
    expected_end = expected_start + len("Java backend experience")

    assert evidence["start"] == expected_start
    assert evidence["end"] == expected_end

    # EvidenceSpan 必须能直接定位调用方提交的原始文本。
    assert original_text[evidence["start"]:evidence["end"]] == evidence["source_text"]
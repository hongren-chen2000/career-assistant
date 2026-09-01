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

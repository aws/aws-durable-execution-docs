@durable_step
def call_external_api(step_context: StepContext, url: str) -> dict:
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        return response.json()
    except requests.Timeout:
        raise  # Let retry handle timeouts
    except requests.HTTPError as e:
        if e.response.status_code >= 500:
            raise  # Retry server errors
        # Don't retry client errors (400-499)
        return {"error": "client_error", "status": e.response.status_code}

@durable_step
def call_external_api(step_context: StepContext, url: str) -> dict:
    # No error handling - all errors cause retry, even permanent ones
    response = requests.get(url)
    return response.json()

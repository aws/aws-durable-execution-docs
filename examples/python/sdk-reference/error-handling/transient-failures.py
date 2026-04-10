from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    StepContext,
)
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import RetryPresets

@durable_step
def call_external_api(step_context: StepContext, endpoint: str) -> dict:
    """Call external API with retry."""
    # API call that might fail transiently
    response = make_http_request(endpoint)
    return response

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle API calls with automatic retry."""
    # Use transient preset for quick retries
    step_config = StepConfig(retry_strategy=RetryPresets.transient())
    
    try:
        result = context.step(
            call_external_api(event["endpoint"]),
            config=step_config,
        )
        return {"status": "success", "data": result}
    except Exception as e:
        # All retries exhausted
        return {"status": "failed", "error": str(e)}

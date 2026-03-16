from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    ValidationError,
)
from aws_durable_execution_sdk_python.config import CallbackConfig

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle SDK validation errors."""
    try:
        # SDK raises ValidationError if timeout is invalid
        callback = context.create_callback(
            config=CallbackConfig(timeout_seconds=-1),  # Invalid!
            name="approval"
        )
        return {"callback_id": callback}
    except ValidationError as e:
        # SDK caught invalid configuration
        return {"error": "InvalidConfiguration", "message": str(e)}

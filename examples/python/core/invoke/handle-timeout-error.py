from aws_durable_execution_sdk_python.config import Duration, InvokeConfig

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle timeout errors."""
    config = InvokeConfig(timeout=Duration.from_seconds(30))
    
    try:
        result = context.invoke(
            function_name="slow-function",
            payload=event,
            config=config,
        )
        return {"status": "success", "result": result}
    
    except CallableRuntimeError as e:
        if "timed out" in str(e).lower():
            context.logger.warning("Function timed out, using fallback")
            return {"status": "timeout", "fallback": True}
        raise

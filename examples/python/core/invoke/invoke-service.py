from aws_durable_execution_sdk_python import durable_step, StepContext

@durable_step
def invoke_service(step_context: StepContext, service_name: str, data: dict) -> dict:
    """Invoke a service and return its result."""
    # Note: This is a simplified example. In practice, you'd need access to context
    # which isn't directly available in step functions.
    return {"service": service_name, "result": data}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Fan out to multiple services."""
    services = ["service-a", "service-b", "service-c"]
    
    # Invoke each service sequentially
    results = []
    for service in services:
        result = context.invoke(
            function_name=service,
            payload=event,
            name=f"invoke_{service}",
        )
        results.append(result)
    
    return {"results": results}

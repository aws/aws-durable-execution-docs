from aws_durable_execution_sdk_python.exceptions import ExecutionError

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    try:
        result = context.step(process_data, complex_object)
    except ExecutionError as e:
        if "Serialization failed" in str(e):
            # Convert to serializable format
            simple_data = convert_to_dict(complex_object)
            result = context.step(process_data, simple_data)
        else:
            raise
    
    return result

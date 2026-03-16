from aws_durable_execution_sdk_python import DurableContext, durable_execution

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Primitives - plain JSON
    none_value = None
    text = "hello"
    number = 42
    decimal_num = 3.14
    flag = True
    
    # Simple lists of primitives - plain JSON
    numbers = [1, 2, 3, 4, 5]
    
    return {
        "none": none_value,
        "text": text,
        "number": number,
        "decimal": decimal_num,
        "flag": flag,
        "numbers": numbers
    }

from aws_durable_execution_sdk_python.config import InvokeConfig
from aws_durable_execution_sdk_python.serdes import SerDes

class CustomSerDes(SerDes):
    """Custom serialization for complex objects."""
    
    def serialize(self, value):
        # Custom serialization logic
        return json.dumps({"custom": value})
    
    def deserialize(self, data: str):
        # Custom deserialization logic
        obj = json.loads(data)
        return obj["custom"]

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Use custom serialization."""
    config = InvokeConfig(
        serdes_payload=CustomSerDes(),
        serdes_result=CustomSerDes(),
    )
    
    result = context.invoke(
        function_name="custom-function",
        payload={"complex": "data"},
        config=config,
    )
    
    return result

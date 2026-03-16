from aws_durable_execution_sdk_python.serdes import SerDes, SerDesContext
import json

class UpperCaseSerDes(SerDes[str]):
    """Example: Convert strings to uppercase during serialization."""
    
    def serialize(self, value: str, serdes_context: SerDesContext) -> str:
        return value.upper()
    
    def deserialize(self, data: str, serdes_context: SerDesContext) -> str:
        return data.lower()

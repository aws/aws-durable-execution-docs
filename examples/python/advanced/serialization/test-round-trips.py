from aws_durable_execution_sdk_python.serdes import serialize, deserialize

def test_serialization():
    original = {"amount": Decimal("99.99")}
    serialized = serialize(None, original, "test-op", "test-arn")
    deserialized = deserialize(None, serialized, "test-op", "test-arn")
    
    assert deserialized == original

from aws_durable_execution_sdk_python.serdes import serialize, deserialize

try:
    serialized = serialize(None, my_data, "test-op", "test-arn")
    deserialized = deserialize(None, serialized, "test-op", "test-arn")
    print("Serialization successful")
except Exception as e:
    print(f"Serialization failed: {e}")

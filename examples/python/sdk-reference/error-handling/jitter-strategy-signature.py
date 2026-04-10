from aws_durable_execution_sdk_python.config import JitterStrategy

class JitterStrategy(StrEnum):
    NONE = "NONE"  # exact calculated delay
    FULL = "FULL"  # random between 0 and base_delay
    HALF = "HALF"  # random between 50% and 100% of base_delay

from aws_durable_execution_sdk_python.config import StepConfig, StepSemantics

step_config = StepConfig(
    step_semantics=StepSemantics.AT_MOST_ONCE_PER_RETRY
)

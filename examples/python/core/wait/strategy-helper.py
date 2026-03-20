from aws_durable_execution_sdk_python.waits import create_wait_strategy, WaitStrategyConfig, JitterStrategy
from aws_durable_execution_sdk_python.config import Duration

strategy = create_wait_strategy(WaitStrategyConfig(
    should_continue_polling=lambda state: state["status"] != "COMPLETED",
    max_attempts=10,
    initial_delay=Duration.from_seconds(5),
    max_delay=Duration.from_minutes(5),
    backoff_rate=2.0,
    jitter_strategy=JitterStrategy.FULL,
))

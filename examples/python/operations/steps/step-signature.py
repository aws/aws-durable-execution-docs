from aws_durable_execution_sdk_python import StepContext
from aws_durable_execution_sdk_python.config import StepConfig


def step(
    func: Callable[[StepContext], T],
    name: str | None = None,
    config: StepConfig | None = None,
) -> T: ...

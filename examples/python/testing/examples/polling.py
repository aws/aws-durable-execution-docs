from dataclasses import dataclass
from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python.types import WaitForConditionCheckContext
from aws_durable_execution_sdk_python.waits import (
    WaitForConditionConfig,
    WaitForConditionDecision,
)
from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@dataclass
class PollState:
    attempts: int
    done: bool


def check(state: PollState, ctx: WaitForConditionCheckContext) -> PollState:
    return PollState(attempts=state.attempts + 1, done=state.attempts >= 2)


def wait_strategy(state: PollState, attempts_made: int) -> WaitForConditionDecision:
    if state.done:
        return WaitForConditionDecision.stop_polling()
    return WaitForConditionDecision.continue_waiting(Duration.from_seconds(1))


@durable_execution
def handler(event, context: DurableContext) -> PollState:
    return context.wait_for_condition(
        check,
        WaitForConditionConfig(
            wait_strategy=wait_strategy,
            initial_state=PollState(attempts=0, done=False),
        ),
        name="poll-job",
    )


def test_polls_until_condition_is_met():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=30)

    assert result.status is InvocationStatus.SUCCEEDED

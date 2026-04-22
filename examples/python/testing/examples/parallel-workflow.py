from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_step
def fetch_a(ctx: StepContext) -> str:
    return "data-a"


@durable_step
def fetch_b(ctx: StepContext) -> str:
    return "data-b"


@durable_step
def fetch_c(ctx: StepContext) -> str:
    return "data-c"


@durable_execution
def handler(event, context: DurableContext) -> list:
    results = context.parallel(
        [
            lambda ctx: ctx.step(fetch_a()),
            lambda ctx: ctx.step(fetch_b()),
            lambda ctx: ctx.step(fetch_c()),
        ],
        name="fetch-all",
    )
    return results.get_succeeded()


def test_executes_branches_in_parallel():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED

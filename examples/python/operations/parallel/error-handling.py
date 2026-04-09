from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import CompletionConfig, ParallelConfig


def task_1(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "ok", name="task-1")


def task_2(ctx: DurableContext) -> str:
    def fail(_):
        raise ValueError("task 2 failed")

    return ctx.step(fail, name="task-2")


def task_3(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "ok", name="task-3")


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    result: BatchResult[str] = context.parallel(
        [task_1, task_2, task_3],
        name="tasks",
        config=ParallelConfig(
            completion_config=CompletionConfig(tolerated_failure_count=1)
        ),
    )
    return {
        "succeeded": result.success_count,
        "failed": result.failure_count,
        "results": result.get_results(),
        "errors": [e.message for e in result.get_errors()],
    }

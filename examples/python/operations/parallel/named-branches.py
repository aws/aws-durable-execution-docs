from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    ParallelBranch,
    durable_execution,
)


def task_a(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "a done", name="run-a")


def task_b(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "b done", name="run-b")


@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    result: BatchResult[str] = context.parallel(
        [
            # A plain callable runs as an unnamed branch
            task_a,
            # Wrap a callable in ParallelBranch to give the branch a name
            ParallelBranch(func=task_b, name="task-b"),
        ],
        name="process",
    )
    return result.get_results()

from aws_durable_execution_sdk_python import (
    DagConfig,
    DagContext,
    DagResult,
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import CompletionConfig
@durable_execution
def handler(event: dict, context: DurableContext) -> int:
    config = DagConfig(
        max_concurrency=2,
        completion_config=CompletionConfig(tolerated_failure_count=1),
    )

    def register(dag: DagContext) -> None:
        dag.step(lambda deps, sc: "a", name="account-a")
        dag.step(lambda deps, sc: "b", name="account-b")
        dag.step(lambda deps, sc: "c", name="account-c")

    result: DagResult = context.dag(register, name="sync-accounts", config=config)
    return result.success_count

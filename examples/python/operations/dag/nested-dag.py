from aws_durable_execution_sdk_python import (
    DagConfig,
    DagContext,
    DagResult,
    DurableContext,
    durable_execution,
)
@durable_execution
def handler(event: dict, context: DurableContext) -> int:
    def register(dag: DagContext) -> None:
        dag.step(lambda deps, sc: "ready", name="prepare")

        def inner(sub: DagContext) -> None:
            sub.step(lambda deps, sc: 1, name="a")
            sub.step(lambda deps, sc: 2, name="b")

        dag.dag(inner, name="inner", config=DagConfig(max_concurrency=5))

    result: DagResult = context.dag(
        register, name="outer", config=DagConfig(max_concurrency=10)
    )
    return result.success_count

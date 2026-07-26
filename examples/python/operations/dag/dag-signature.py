from aws_durable_execution_sdk_python import (
    DagContext,
    DagResult,
    DurableContext,
    durable_execution,
)
@durable_execution
def handler(event: dict, context: DurableContext) -> int:
    def register(dag: DagContext) -> None:
        dag.step(lambda deps, sc: 42, name="double")

    result: DagResult = context.dag(register, name="compute")
    return result.get_result("double")

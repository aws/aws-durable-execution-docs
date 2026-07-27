from aws_durable_execution_sdk_python import (
    DagContext,
    DagResult,
    DurableContext,
    durable_execution,
)


@durable_execution
def handler(event: dict, context: DurableContext) -> int:
    def register(dag: DagContext) -> None:
        seed = dag.step(lambda deps, sc: 10, name="seed")
        left = dag.step(lambda deps, sc: deps["seed"] + 1, deps=[seed], name="left")
        right = dag.step(lambda deps, sc: deps["seed"] * 2, deps=[seed], name="right")
        dag.step(
            lambda deps, sc: deps["left"] + deps["right"],
            deps=[left, right],
            name="merge",
        )

    result: DagResult = context.dag(register, name="diamond")
    return result.get_result("merge")

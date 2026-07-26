from aws_durable_execution_sdk_python import (
    DagContext,
    DagResult,
    DurableContext,
    durable_execution,
)
@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    def register(dag: DagContext) -> None:
        users = dag.step(
            lambda deps, sc: [{"id": "u1"}, {"id": "u2"}], name="fetch-users"
        )
        orders = dag.step(lambda deps, sc: [{"id": "o1"}], name="fetch-orders")
        dag.step(
            lambda deps, sc: {
                "users": len(deps["fetch-users"]),
                "orders": len(deps["fetch-orders"]),
            },
            deps=[users, orders],
            name="join",
        )

    result: DagResult = context.dag(register, name="load-and-join")
    return result.get_result("join")

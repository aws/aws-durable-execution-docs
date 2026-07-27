from aws_durable_execution_sdk_python import (
    DagContext,
    DagResult,
    DurableContext,
    durable_execution,
)


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    def register(dag: DagContext) -> None:
        order = dag.step(lambda deps, sc: {"total": 0}, name="load-order")
        refund = dag.step(
            lambda deps, sc: f"refunded {deps['load-order']['total']}",
            deps=[order],
            name="issue-refund",
            run_if=lambda deps: deps["load-order"]["total"] > 0,
        )
        dag.step(
            lambda deps, sc: f"sent: {deps['issue-refund']}",
            deps=[refund],
            name="notify",
        )

    result: DagResult = context.dag(register, name="conditional-refund")
    return {
        "refund": result.get_status("issue-refund").value,
        "notify": result.get_status("notify").value,
    }

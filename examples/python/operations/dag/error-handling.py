from aws_durable_execution_sdk_python import (
    DagContext,
    DagResult,
    DurableContext,
    durable_execution,
)


def _charge_card(deps, sc):
    raise RuntimeError("card declined")


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    def register(dag: DagContext) -> None:
        charge = dag.step(_charge_card, name="charge-card")
        dag.step(lambda deps, sc: "shipped", deps=[charge], name="ship-order")
        dag.step(lambda deps, sc: "recorded", name="record-metrics")

    result: DagResult = context.dag(register, name="charge-and-ship")
    failures = [
        {"name": task.name, "error": task.error.message if task.error else None}
        for task in result.failed()
    ]
    return {
        "failures": failures,
        "shipStatus": result.get_status("ship-order").value,
        "metricsStatus": result.get_status("record-metrics").value,
    }

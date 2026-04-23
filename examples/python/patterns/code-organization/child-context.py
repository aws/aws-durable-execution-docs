from aws_durable_execution_sdk_python import durable_with_child_context


@durable_with_child_context
def process_order(child: DurableContext, order: dict) -> str:
    child.step(validate_step(order))
    child.step(charge_step(order))
    child.step(schedule_step(order))
    return "ok"


context.run_in_child_context(process_order(order), name="process-order")

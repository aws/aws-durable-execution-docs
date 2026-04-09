from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)


def process_user(
    ctx: DurableContext, user_id: str, index: int, user_ids: list[str]
) -> str:
    return ctx.step(lambda _: f"processed-{user_id}", name=f"process-{index}")


@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    # Pass name as keyword argument; omit or pass None to leave unnamed
    result: BatchResult[str] = context.map(
        event["userIds"],
        process_user,
        name="process-users",
    )
    return result.to_dict()

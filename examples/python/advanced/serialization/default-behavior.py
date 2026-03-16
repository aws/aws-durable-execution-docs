from aws_durable_execution_sdk_python import DurableContext, durable_execution
from datetime import datetime
from decimal import Decimal
from uuid import uuid4

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # All these types serialize automatically
    result = context.step(
        process_order,
        order_id=uuid4(),
        amount=Decimal("99.99"),
        timestamp=datetime.now()
    )
    return result

from dataclasses import dataclass, asdict
from aws_durable_execution_sdk_python import DurableContext, durable_execution

@dataclass
class Order:
    order_id: str
    amount: float
    customer: str

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    order = Order(
        order_id="ORD-123",
        amount=99.99,
        customer="Jane Doe"
    )
    
    # Convert to dict before passing to step
    result = context.step(process_order, asdict(order))
    return result

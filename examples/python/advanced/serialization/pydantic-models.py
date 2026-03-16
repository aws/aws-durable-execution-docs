from pydantic import BaseModel
from aws_durable_execution_sdk_python import DurableContext, durable_execution

class Order(BaseModel):
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
    
    # Use model_dump() to convert to dict
    result = context.step(process_order, order.model_dump())
    return result

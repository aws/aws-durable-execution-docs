from aws_durable_execution_sdk_python import DurableContext, durable_execution

class Order:
    def __init__(self, order_id: str, amount: float, customer: str):
        self.order_id = order_id
        self.amount = amount
        self.customer = customer
    
    def to_dict(self) -> dict:
        return {
            "order_id": self.order_id,
            "amount": self.amount,
            "customer": self.customer
        }
    
    @classmethod
    def from_dict(cls, data: dict) -> "Order":
        return cls(
            order_id=data["order_id"],
            amount=data["amount"],
            customer=data["customer"]
        )

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    order = Order("ORD-123", 99.99, "Jane Doe")
    
    # Convert to dict before passing to step
    result = context.step(process_order, order.to_dict())
    return result

from dataclasses import dataclass, asdict

@dataclass
class Order:
    order_id: str
    total: float
    items: list

@durable_step
def process_order(step_context: StepContext, order_data: dict) -> dict:
    order = Order(**order_data)
    # Process order...
    return asdict(order)  # Convert dataclass to dict

from datetime import datetime
from decimal import Decimal

@durable_step
def process_order(step_context: StepContext, order: dict) -> dict:
    return {
        "order_id": order["id"],
        "total": float(Decimal("99.99")),  # Convert to float
        "timestamp": datetime.now().isoformat(),  # Convert to string
    }

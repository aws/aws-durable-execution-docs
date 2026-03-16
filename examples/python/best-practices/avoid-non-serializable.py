from datetime import datetime
from decimal import Decimal

@durable_step
def process_order(step_context: StepContext, order: dict) -> dict:
    # datetime and Decimal aren't JSON-serializable by default
    return {
        "order_id": order["id"],
        "total": Decimal("99.99"),  # Won't serialize!
        "timestamp": datetime.now(),  # Won't serialize!
    }

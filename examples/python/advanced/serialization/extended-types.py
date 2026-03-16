from aws_durable_execution_sdk_python import DurableContext, durable_execution
from datetime import datetime, date
from decimal import Decimal
from uuid import UUID, uuid4

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Extended types - automatic serialization
    order_data = {
        "order_id": uuid4(),                    # UUID
        "amount": Decimal("99.99"),             # Decimal
        "created_at": datetime.now(),           # datetime
        "delivery_date": date.today(),          # date
        "signature": b"binary_signature_data",  # bytes
        "coordinates": (40.7128, -74.0060),     # tuple
    }
    
    result = context.step(process_order, order_data)
    return result

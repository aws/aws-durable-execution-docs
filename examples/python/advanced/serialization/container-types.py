from aws_durable_execution_sdk_python import DurableContext, durable_execution
from datetime import datetime
from decimal import Decimal
from uuid import uuid4

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Nested structures serialize automatically
    complex_data = {
        "user": {
            "id": uuid4(),
            "created": datetime.now(),
            "balance": Decimal("1234.56"),
            "metadata": b"binary_data",
            "coordinates": (40.7128, -74.0060),
            "tags": ["premium", "verified"],
            "settings": {
                "notifications": True,
                "theme": "dark",
                "limits": {
                    "daily": Decimal("500.00"),
                    "monthly": Decimal("10000.00"),
                },
            },
        }
    }
    
    result = context.step(process_user, complex_data)
    return result

# Good - uses default serialization
from datetime import datetime
from decimal import Decimal

result = context.step(
    process_order,
    order_id="ORD-123",
    amount=Decimal("99.99"),
    timestamp=datetime.now()
)

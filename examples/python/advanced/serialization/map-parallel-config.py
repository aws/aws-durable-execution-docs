from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import MapConfig

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    items = [1, 2, 3, 4, 5]
    
    # Custom serialization for BatchResult
    config = MapConfig(
        serdes=CustomSerDes(),        # For the entire BatchResult
        item_serdes=ItemSerDes()      # For individual item results
    )
    
    result = context.map(process_item, items, config=config)
    return {"processed": len(result.succeeded)}

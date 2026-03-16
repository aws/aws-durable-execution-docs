def check_inventory(
    context: DurableContext,
    product_id: str,
    index: int,
    products: list[str]
) -> dict:
    """Check if a product is in stock."""
    # Check if product is in stock
    return {"product_id": product_id, "in_stock": True, "quantity": 10}

@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    product_ids = ["prod_1", "prod_2", "prod_3", "prod_4"]
    
    # Get all inventory results
    batch_result = context.map(product_ids, check_inventory)
    
    # Filter to only in-stock products
    in_stock = [
        r.result["product_id"]
        for r in batch_result.results
        if r.result["in_stock"]
    ]
    
    return in_stock

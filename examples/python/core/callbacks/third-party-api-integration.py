@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Integrate with third-party data enrichment API."""
    user_data = event["user_data"]
    
    # Create callback for enrichment result
    enrichment_callback = context.create_callback(
        name="data_enrichment",
        config=CallbackConfig(timeout=Duration.from_minutes(10)),
    )
    
    # Request data enrichment from third-party
    request_data_enrichment({
        "callback_id": enrichment_callback.callback_id,
        "user_data": user_data,
        "webhook_url": f"https://api.example.com/webhooks/{enrichment_callback.callback_id}",
    })
    
    # Wait for enriched data
    enriched_data = enrichment_callback.result()
    
    # Combine original and enriched data
    return {
        "original": user_data,
        "enriched": enriched_data,
        "timestamp": enriched_data.get("processed_at"),
    }

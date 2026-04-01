var userData = event.get("user_data");

// Create callback for enrichment result
var enrichmentCallback = ctx.createCallback("data_enrichment", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofMinutes(10))
        .build());

// Request data enrichment from third-party
ctx.step("request_enrichment", Void.class, stepCtx -> {
    requestDataEnrichment(Map.of(
        "callback_id", enrichmentCallback.callbackId(),
        "user_data", userData,
        "webhook_url", "https://api.example.com/webhooks/" + enrichmentCallback.callbackId()));
    return null;
});

// Wait for enriched data
var enrichedData = enrichmentCallback.get();

// Combine original and enriched data
return Map.of(
    "original", userData,
    "enriched", enrichedData,
    "timestamp", enrichedData.get("processed_at"));

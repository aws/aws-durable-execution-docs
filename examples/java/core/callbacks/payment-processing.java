var amount = event.get("amount");
var customerId = (String) event.get("customer_id");

// Create callback for payment result
var paymentCallback = ctx.createCallback("payment_processing", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofMinutes(5))
        .heartbeatTimeout(Duration.ofSeconds(30))
        .build());

// Initiate payment with external processor
ctx.step("initiate_payment", Void.class, stepCtx -> {
    initiatePaymentWithProcessor(Map.of(
        "callback_id", paymentCallback.callbackId(),
        "amount", amount,
        "customer_id", customerId,
        "callback_url", "https://api.example.com/callbacks/" + paymentCallback.callbackId()));
    return null;
});

// Wait for payment result
var paymentResult = paymentCallback.get();

return Map.of(
    "transaction_id", paymentResult.get("transaction_id"),
    "status", paymentResult.get("status"),
    "amount", amount);

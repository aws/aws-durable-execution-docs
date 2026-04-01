// Create callback for payment processing
var callback = ctx.createCallback("payment-callback", String.class,
    CallbackConfig.builder()
        .timeout(Duration.ofMinutes(5))
        .build());

// Send to message broker (SQS, SNS, EventBridge, etc.)
ctx.step("send-to-queue", Void.class, stepCtx -> {
    sendToPaymentQueue(Map.of(
        "callback_id", callback.callbackId(),
        "amount", input.get("amount"),
        "customer_id", input.get("customer_id")));
    return null;
});

// Wait for result — execution suspends here
var paymentResult = callback.get();

// Execution resumes when callback is notified
return Map.of(
    "payment_status", paymentResult,
    "callback_id", callback.callbackId());

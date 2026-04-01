// Create callback
var callback = ctx.createCallback("payment_callback", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofMinutes(5))
        .build());

// Send to payment processor
ctx.step("initiate_payment", Void.class,
    stepCtx -> {
        initiatePayment(callback.callbackId(), event.get("amount"));
        return null;
    });

// Wait for payment result
var paymentResult = callback.get();

return Map.of("payment_status", paymentResult);

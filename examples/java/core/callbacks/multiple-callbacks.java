// Create callbacks for different systems
var creditCheck = ctx.createCallback("credit_check", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofMinutes(5))
        .build());

var fraudCheck = ctx.createCallback("fraud_check", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofMinutes(3))
        .build());

// Send requests to external systems
ctx.step("request_checks", Void.class, stepCtx -> {
    requestCreditCheck(creditCheck.callbackId(), event.get("customer_id"));
    requestFraudCheck(fraudCheck.callbackId(), event.get("transaction_data"));
    return null;
});

// Wait for both results
var creditResult = creditCheck.get();
var fraudResult = fraudCheck.get();

// Make decision based on both checks
var approved = ((Number) creditResult.get("score")).intValue() > 650
    && "low".equals(fraudResult.get("risk_level"));

return Map.of(
    "approved", approved,
    "credit_score", creditResult.get("score"),
    "fraud_risk", fraudResult.get("risk_level"));

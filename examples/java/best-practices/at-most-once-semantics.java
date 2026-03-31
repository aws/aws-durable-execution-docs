// Prevent duplicate charges on retry
var payment = ctx.step("charge_credit_card", Map.class, stepCtx -> {
    return Map.of("transaction_id", "txn_123", "status", "completed");
}, StepConfig.builder()
    .semantics(StepSemantics.AT_MOST_ONCE_PER_RETRY)
    .build());
return payment;

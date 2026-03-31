public class ChargeCreditCardHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Use at-most-once to prevent duplicate charges
        var stepConfig = StepConfig.builder()
            .semantics(StepSemantics.AT_MOST_ONCE_PER_RETRY)
            .build();

        var payment = ctx.step("charge-credit-card", Map.class, stepCtx -> {
            // Payment processing logic
            return Map.of("transaction_id", "txn_123", "status", "completed");
        }, stepConfig);

        return payment;
    }
}

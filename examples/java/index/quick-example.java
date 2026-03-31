public class ProcessOrderHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        var orderId = (String) event.get("order_id");
        var amount = (Double) event.get("amount");

        // Step 1: Validate the order
        var validation = ctx.step("validate-order", Map.class, stepCtx -> {
            // Validation logic here
            return Map.of("order_id", orderId, "valid", true);
        });

        if (!(boolean) validation.get("valid")) {
            return Map.of("status", "failed", "reason", "invalid_order");
        }

        // Step 2: Charge payment
        var payment = ctx.step("charge-payment", Map.class, stepCtx -> {
            // Payment processing logic here
            return Map.of("transaction_id", "txn_123", "status", "completed");
        });

        // Step 3: Wait for payment confirmation (simulated)
        ctx.wait("payment-confirmation", Duration.ofSeconds(5));

        // Step 4: Fulfill the order
        var fulfillment = ctx.step("fulfill-order", Map.class, stepCtx -> {
            // Fulfillment logic here
            return Map.of("tracking_number", "TRK123456");
        });

        return Map.of(
            "status", "completed",
            "order_id", orderId,
            "transaction_id", payment.get("transaction_id"),
            "tracking_number", fulfillment.get("tracking_number")
        );
    }
}

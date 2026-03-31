public class ProcessOrderHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        String orderId = (String) event.get("order_id");
        double amount = (double) event.get("amount");

        // Invoke validation function
        var validationResult = ctx.invoke(
            "validate_order",
            "validate-order",
            Map.of("order_id", orderId),
            Map.class
        );

        if (!(boolean) validationResult.get("valid")) {
            return Map.of("status", "rejected", "reason", validationResult.get("reason"));
        }

        // Invoke payment function
        var paymentResult = ctx.invoke(
            "process_payment",
            "process-payment",
            Map.of("order_id", orderId, "amount", amount),
            Map.class
        );

        return Map.of(
            "status", "completed",
            "order_id", orderId,
            "transaction_id", paymentResult.get("transaction_id")
        );
    }
}

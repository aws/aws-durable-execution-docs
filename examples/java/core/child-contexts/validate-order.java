public class ValidateOrderHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        String orderId = (String) event.get("order_id");

        // Once this completes, it never replays — even if the function continues
        var result = ctx.runInChildContext("order_processing", Map.class, childCtx -> {
            // These three steps execute as a single unit
            var validation = childCtx.step("validate", Map.class,
                stepCtx -> Map.of("valid", true, "order_id", orderId));

            var inventory = childCtx.step("reserve_inventory", Map.class,
                stepCtx -> Map.of("reserved", true, "order_id", orderId));

            var payment = childCtx.step("charge_payment", Map.class,
                stepCtx -> Map.of("charged", true, "order_id", orderId));

            return Map.of("order_id", orderId, "status", "completed");
        });

        // Additional operations here won't cause order_processing to replay
        ctx.step("send_confirmation", Map.class,
            stepCtx -> Map.of("sent", true, "order_id", result.get("order_id")));

        return result;
    }
}

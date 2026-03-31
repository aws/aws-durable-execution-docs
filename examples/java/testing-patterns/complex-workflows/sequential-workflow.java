// Sequential workflow handler
public class SequentialHandler extends DurableHandler<Map, Map> {
    @Override
    public Map handleRequest(Map input, DurableContext ctx) {
        String orderId = (String) input.get("order_id");

        Map validated = ctx.step("validate", Map.class,
            stepCtx -> Map.of("order_id", orderId, "status", "validated"));

        Map paid = ctx.step("payment", Map.class, stepCtx -> {
            var result = new HashMap<>(validated);
            result.put("payment_status", "completed");
            return result;
        });

        Map fulfilled = ctx.step("fulfillment", Map.class, stepCtx -> {
            var result = new HashMap<>(paid);
            result.put("fulfillment_status", "shipped");
            return result;
        });

        return fulfilled;
    }
}

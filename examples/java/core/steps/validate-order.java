public class ValidateOrderHandler extends DurableHandler<Map<String, String>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, DurableContext ctx) {
        var orderId = event.get("order_id");

        var validation = ctx.step("validate-order", Map.class, stepCtx -> {
            // Your validation logic here
            return Map.of("order_id", orderId, "valid", true);
        });

        return validation;
    }
}

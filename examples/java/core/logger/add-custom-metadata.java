public class AddCustomMetadataHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        String orderId = (String) event.get("order_id");

        // Add custom metadata via MDC
        MDC.put("order_id", orderId);
        MDC.put("customer_id", (String) event.get("customer_id"));
        MDC.put("priority", "high");

        ctx.getLogger().info("Processing order");

        String result = ctx.step("process_order", String.class,
            stepCtx -> "order-" + orderId + "-processed"
        );

        ctx.getLogger().info("Order completed, result={}", result);
        return result;
    }
}

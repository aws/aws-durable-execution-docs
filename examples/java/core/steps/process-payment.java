public class ProcessPaymentHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Step is explicitly named "process-payment"
        var result = ctx.step("process-payment", Map.class, stepCtx -> {
            return Map.of("status", "completed", "amount", 100.0);
        });
        return result;
    }
}

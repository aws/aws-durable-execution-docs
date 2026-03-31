public class AutoContextHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        // This log includes: durableExecutionArn
        ctx.getLogger().info("Top-level log");

        String result = ctx.step("process_data", String.class, stepCtx -> {
            // This log includes: durableExecutionArn, operationId, operationName, attempt
            stepCtx.getLogger().info("Step completed");
            return "processed";
        });

        return result;
    }
}

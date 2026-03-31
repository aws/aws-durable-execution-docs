public class BasicUsageHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Log at the top level
        ctx.getLogger().info("Starting workflow, event_id={}", event.get("id"));

        // Execute a step
        String result = ctx.step("process_data", String.class, stepCtx -> "processed");

        ctx.getLogger().info("Workflow completed, result={}", result);
        return result;
    }
}

public class DeduplicationHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        // This log appears only once, even if the function is replayed
        ctx.getLogger().info("Starting workflow");

        // Step 1 - logs appear only once
        String result1 = ctx.step("step_1", String.class, stepCtx -> "step1-done");
        ctx.getLogger().info("Step 1 completed, result={}", result1);

        // Step 2 - logs appear only once
        String result2 = ctx.step("step_2", String.class, stepCtx -> "step2-done");
        ctx.getLogger().info("Step 2 completed, result={}", result2);

        return result1 + "-" + result2;
    }
}

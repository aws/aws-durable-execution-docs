// Java equivalent: ctx.getLogger() provides BOTH features out of the box:
// - Structured logging via SLF4J MDC (JSON output with Log4j2/Logback)
// - Log deduplication during replays

public class DeduplicatedLoggingHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Replay-aware logger with automatic MDC enrichment
        ctx.getLogger().info("Starting workflow");

        String result1 = ctx.step("step_1", String.class, stepCtx -> "step1-done");
        ctx.getLogger().info("Step 1 completed, result={}", result1);

        String result2 = ctx.step("step_2", String.class, stepCtx -> "step2-done");
        ctx.getLogger().info("Step 2 completed, result={}", result2);

        return result1 + "-" + result2;
    }
}

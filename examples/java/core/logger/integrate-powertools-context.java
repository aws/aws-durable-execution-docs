// Java equivalent: MDC context enrichment
// The SDK automatically enriches logs via SLF4J MDC — no manual setup needed.
// Any SLF4J-compatible framework (Log4j2, Logback) picks up MDC fields automatically.

public class EnrichedLoggingHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Add custom business context via MDC
        MDC.put("service", "order-processing");
        MDC.put("event_id", (String) event.get("id"));

        // ctx.getLogger() already enriches with durableExecutionArn, requestId, etc.
        ctx.getLogger().info("Starting workflow");

        String result = ctx.step("process_data", String.class, stepCtx -> "processed");

        ctx.getLogger().info("Workflow completed, result={}", result);
        return result;
    }
}

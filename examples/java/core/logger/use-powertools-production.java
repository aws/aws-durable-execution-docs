// Java equivalent: use ctx.getLogger() for production logging.
// It provides JSON-structured logs with automatic MDC enrichment
// and replay-aware deduplication — no extra setup needed.

public class ProductionHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Structured JSON logs with all SDK context fields
        ctx.getLogger().info("Processing started");
        // ...
    }
}

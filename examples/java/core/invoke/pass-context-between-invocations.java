public class PassContextHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // First invocation creates context
        var initialContext = ctx.invoke(
            "initialize",
            "initialize-context",
            event,
            Map.class
        );

        // Second invocation uses the context
        var processed = ctx.invoke(
            "process",
            "process-with-context",
            Map.of("data", event.get("data"), "context", initialContext),
            Map.class
        );

        // Third invocation finalizes
        var finalResult = ctx.invoke(
            "finalize",
            "finalize",
            Map.of("processed", processed, "context", initialContext),
            Map.class
        );

        return finalResult;
    }
}

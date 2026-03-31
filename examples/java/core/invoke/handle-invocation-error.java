public class HandleInvocationErrorHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Handle errors from invoked functions
        try {
            var result = ctx.invoke(
                "risky_operation",
                "risky-function",
                event,
                Map.class
            );
            return Map.of("status", "success", "result", result);

        } catch (InvokeFailedException e) {
            // Handle the error from the invoked function
            ctx.getLogger().error("Invoked function failed: {}", e.getMessage());
            return Map.of(
                "status", "failed",
                "error", e.getMessage()
            );
        }
    }
}

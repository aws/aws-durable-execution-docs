public class RetryExampleHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Retry failed invocations
        int maxRetries = 3;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                var result = ctx.invoke(
                    "attempt_" + (attempt + 1),
                    "unreliable-function",
                    event,
                    Map.class
                );
                return Map.of("status", "success", "result", result, "attempts", attempt + 1);

            } catch (InvokeFailedException e) {
                if (attempt == maxRetries - 1) {
                    // Last attempt failed
                    return Map.of(
                        "status", "failed",
                        "error", e.getMessage(),
                        "attempts", maxRetries
                    );
                }
                // Wait before retrying (exponential backoff)
                ctx.wait("retry_wait_" + attempt, Duration.ofSeconds((long) Math.pow(2, attempt)));
            }
        }

        return Map.of("status", "failed", "reason", "max_retries_exceeded");
    }
}

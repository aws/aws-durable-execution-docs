// Retry behavior workflow handler
public class RetryHandler extends DurableHandler<Map, String> {
    static int attemptCount = 0;

    @Override
    public String handleRequest(Map input, DurableContext ctx) {
        var retryConfig = StepConfig.builder()
            .retryStrategy(RetryStrategies.builder()
                .maxAttempts(5)
                .retryableExceptions(List.of(RuntimeException.class))
                .build())
            .build();

        String result = ctx.step("unreliable", String.class, retryConfig, stepCtx -> {
            attemptCount++;
            if (attemptCount < 3) {
                throw new RuntimeException("Transient error");
            }
            return "Operation succeeded";
        });

        return result;
    }
}

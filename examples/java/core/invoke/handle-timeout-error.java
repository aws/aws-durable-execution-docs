public class HandleTimeoutErrorHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Handle timeout errors
        InvokeConfig config = InvokeConfig.builder()
            .timeout(Duration.ofSeconds(30))
            .build();

        try {
            var result = ctx.invoke(
                "slow_operation",
                "slow-function",
                event,
                Map.class,
                config
            );
            return Map.of("status", "success", "result", result);

        } catch (InvokeTimedOutException e) {
            ctx.getLogger().warn("Function timed out, using fallback");
            return Map.of("status", "timeout", "fallback", true);

        } catch (InvokeFailedException e) {
            throw e;
        }
    }
}

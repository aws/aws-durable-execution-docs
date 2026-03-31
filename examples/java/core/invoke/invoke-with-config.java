public class InvokeWithConfigHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Configure invoke with timeout
        InvokeConfig config = InvokeConfig.builder()
            .timeout(Duration.ofMinutes(5))
            .build();

        var result = ctx.invoke(
            "long_running",
            "long-running-function",
            event,
            Map.class,
            config
        );

        return result;
    }
}

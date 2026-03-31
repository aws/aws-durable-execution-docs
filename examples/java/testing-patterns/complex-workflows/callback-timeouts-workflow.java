// Callback timeouts workflow handler
public class CallbackTimeoutHandler extends DurableHandler<Map, String> {
    @Override
    public String handleRequest(Map input, DurableContext ctx) {
        var config = CallbackConfig.builder()
            .timeout(Duration.ofSeconds(60))
            .heartbeatTimeout(Duration.ofSeconds(30))
            .build();

        var callback = ctx.createCallback("approval_callback", String.class, config);
        return "Callback created: " + callback.getCallbackId();
    }
}

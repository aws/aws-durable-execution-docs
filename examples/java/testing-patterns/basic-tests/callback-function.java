public class CallbackHandler extends DurableHandler<Map, String> {
    @Override
    public String handleRequest(Map input, DurableContext ctx) {
        var callbackConfig = CallbackConfig.builder()
            .timeout(Duration.ofSeconds(120))
            .heartbeatTimeout(Duration.ofSeconds(60))
            .build();

        var callback = ctx.createCallback("example_callback", String.class, callbackConfig);
        return "Callback created with ID: " + callback.getCallbackId();
    }
}

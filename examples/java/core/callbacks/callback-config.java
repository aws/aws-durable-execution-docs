import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.callback.CallbackConfig;

public class CallbackConfigHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Configure callback with custom timeouts
        var config = CallbackConfig.builder()
            .timeout(Duration.ofSeconds(60))
            .heartbeatTimeout(Duration.ofSeconds(30))
            .build();

        var callback = ctx.createCallback("timeout_callback", String.class, config);

        return "Callback created with 60s timeout: " + callback.callbackId();
    }
}

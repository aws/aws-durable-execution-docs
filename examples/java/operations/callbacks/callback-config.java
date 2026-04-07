import java.time.Duration;
import software.amazon.lambda.durable.DurableCallbackFuture;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.CallbackConfig;

public class CallbackConfigExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        CallbackConfig config = CallbackConfig.builder()
                .timeout(Duration.ofHours(24))
                .heartbeatTimeout(Duration.ofMinutes(30))
                .build();

        DurableCallbackFuture<String> callback =
                context.createCallback("wait-for-payment", String.class, config);

        submitPaymentRequest(callback.callbackId());
        return callback.get();
    }
}

import java.util.Map;
import software.amazon.lambda.durable.DurableCallbackFuture;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.CallbackConfig;
import software.amazon.lambda.durable.serde.JacksonSerDes;

public class CallbackConfigExample extends DurableHandler<Object, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Object input, DurableContext context) {
        CallbackConfig config = CallbackConfig.builder()
            .serDes(new JacksonSerDes())
            .build();

        DurableCallbackFuture<Map<String, Object>> callback =
            context.createCallback("await-approval", Map.class, config);

        // Send callback.getCallbackId() to the external system here.
        return callback.get();
    }
}

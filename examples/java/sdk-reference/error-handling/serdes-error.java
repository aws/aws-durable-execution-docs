import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.exception.SerDesException;

public class SerdesErrorExample extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext context) {
        // SerDesException is thrown when the SDK cannot serialize or deserialize a value.
        // Custom SerDes implementations should throw SerDesException when they encounter
        // a value they cannot handle.
        try {
            Map<String, Object> result = context.step("build-result", Map.class, stepCtx ->
                    Map.of("message", "hello"));
            return result;
        } catch (SerDesException e) {
            context.getLogger().error("Serialization failed: " + e.getMessage());
            throw e;
        }
    }
}

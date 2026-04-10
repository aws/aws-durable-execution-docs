import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.exception.SerDesException;
import software.amazon.lambda.durable.serde.SerDes;

// A pass-through SerDes stores the value as-is (already a JSON string).
class PassThroughSerDes implements SerDes {
    @Override
    public String serialize(Object value) {
        return (String) value;
    }

    @Override
    public <T> T deserialize(String data, TypeToken<T> typeToken) {
        @SuppressWarnings("unchecked")
        T result = (T) data;
        return result;
    }
}

public class PassThroughSerdesExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        StepConfig config = StepConfig.builder()
            .serDes(new PassThroughSerDes())
            .build();

        return context.step(
            "fetch-raw",
            String.class,
            (StepContext ctx) -> "{\"id\":\"order-123\"}",
            config
        );
    }
}

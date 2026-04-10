import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.serde.JacksonSerDes;

public class StepConfigExample extends DurableHandler<Object, Map<String, String>> {
    @Override
    public Map<String, String> handleRequest(Object input, DurableContext context) {
        StepConfig config = StepConfig.builder()
            .serDes(new JacksonSerDes())
            .build();

        Map<String, String> order = context.step(
            "fetch-order",
            Map.class,
            (StepContext ctx) -> Map.of("id", "order-123", "total", "99.99"),
            config
        );
        return order;
    }
}

import java.util.List;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.MapConfig;
import software.amazon.lambda.durable.model.MapResult;
import software.amazon.lambda.durable.serde.JacksonSerDes;

public class MapConfigExample extends DurableHandler<Object, List<Map<String, String>>> {
    @Override
    public List<Map<String, String>> handleRequest(Object input, DurableContext context) {
        MapConfig config = MapConfig.builder()
            .serDes(new JacksonSerDes())
            .build();

        List<String> items = List.of("a", "b", "c");
        MapResult<Map<String, String>> result = context.map(
            "process-items",
            items,
            Map.class,
            (item, index, ctx) -> Map.of("id", item, "status", "done"),
            config
        );
        return result.succeeded();
    }
}

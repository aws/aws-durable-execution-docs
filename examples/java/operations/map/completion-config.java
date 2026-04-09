import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.CompletionConfig;
import software.amazon.lambda.durable.config.MapConfig;
import software.amazon.lambda.durable.model.MapResult;

public class MapCompletionConfig extends DurableHandler<List<String>, List<String>> {
    @Override
    public List<String> handleRequest(List<String> items, DurableContext context) {
        var config = MapConfig.builder()
                .completionConfig(CompletionConfig.minSuccessful(3))
                .build();

        MapResult<String> result = context.map(
                "process-items",
                items,
                String.class,
                (item, index, ctx) -> ctx.step(
                        "process-" + index, String.class, s -> item.toUpperCase()),
                config);

        return result.succeeded();
    }
}

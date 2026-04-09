import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.model.MapResult;

public class NamedMap extends DurableHandler<List<String>, List<String>> {
    @Override
    public List<String> handleRequest(List<String> userIds, DurableContext context) {
        // The name is always required in Java
        MapResult<String> result = context.map(
                "process-users",
                userIds,
                String.class,
                (userId, index, ctx) -> ctx.step(
                        "process-" + index, String.class, s -> "processed-" + userId));

        return result.succeeded();
    }
}

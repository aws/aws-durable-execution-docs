import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.model.MapResult;

public class MapErrorHandling extends DurableHandler<List<String>, Void> {
    @Override
    public Void handleRequest(List<String> items, DurableContext context) {
        MapResult<String> result = context.map(
                "process-items",
                items,
                String.class,
                (item, index, ctx) -> ctx.step("process-" + index, String.class, s -> {
                    if ("bad".equals(item)) throw new IllegalArgumentException("bad item");
                    return item.toUpperCase();
                }));

        var failures = result.failed();
        if (!failures.isEmpty()) {
            System.out.println(failures.size() + " items failed");
            failures.forEach(e -> System.out.println(e.errorType() + ": " + e.errorMessage()));
        }

        var successes = result.succeeded();
        System.out.println(successes.size() + " items succeeded: " + successes);
        return null;
    }
}

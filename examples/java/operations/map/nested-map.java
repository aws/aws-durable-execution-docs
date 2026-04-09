import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.model.MapResult;

public class NestedMap extends DurableHandler<List<Region>, List<List<String>>> {
    record Region(String name, List<String> items) {}

    @Override
    public List<List<String>> handleRequest(List<Region> regions, DurableContext context) {
        MapResult<List<String>> result = context.map(
                "process-regions",
                regions,
                new TypeToken<List<String>>() {},
                (region, index, ctx) -> {
                    MapResult<String> inner = ctx.map(
                            "process-" + region.name(),
                            region.items(),
                            String.class,
                            (item, i, innerCtx) -> innerCtx.step(
                                    "item-" + i, String.class, s -> item.toUpperCase()));
                    return inner.succeeded();
                });

        return result.succeeded();
    }
}

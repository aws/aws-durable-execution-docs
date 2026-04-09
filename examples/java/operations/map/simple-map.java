import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.model.MapResult;

public class SimpleMap extends DurableHandler<Void, List<Integer>> {
    @Override
    public List<Integer> handleRequest(Void input, DurableContext context) {
        MapResult<Integer> result = context.map(
                "square-numbers",
                List.of(1, 2, 3, 4, 5),
                Integer.class,
                (item, index, ctx) -> ctx.step(
                        "square-" + index, Integer.class, s -> item * item));

        return result.results();
    }
}

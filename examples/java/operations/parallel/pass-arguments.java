import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.model.ParallelResult;

public class PassArguments extends DurableHandler<Void, ParallelResult> {
    @Override
    public ParallelResult handleRequest(Void input, DurableContext context) {
        var items = List.of("a", "b", "c");

        try (var parallel = context.parallel("process-items")) {
            for (var item : items) {
                parallel.branch("process-" + item, String.class,
                        ctx -> ctx.step("run-" + item, String.class, s -> "processed " + item));
            }
            return parallel.get();
        }
    }
}

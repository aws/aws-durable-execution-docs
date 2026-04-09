import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.model.ParallelResult;

public class NamedBranches extends DurableHandler<Void, ParallelResult> {
    @Override
    public ParallelResult handleRequest(Void input, DurableContext context) {
        try (var parallel = context.parallel("process")) {
            parallel.branch("task-a", String.class,
                    ctx -> ctx.step("run-a", String.class, s -> "a done"));
            parallel.branch("task-b", String.class,
                    ctx -> ctx.step("run-b", String.class, s -> "b done"));
            return parallel.get();
        }
    }
}

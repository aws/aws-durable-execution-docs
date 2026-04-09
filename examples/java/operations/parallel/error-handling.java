import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.CompletionConfig;
import software.amazon.lambda.durable.config.ParallelConfig;
import software.amazon.lambda.durable.model.ParallelResult;

public class ErrorHandling extends DurableHandler<Void, ParallelResult> {
    @Override
    public ParallelResult handleRequest(Void input, DurableContext context) {
        var config = ParallelConfig.builder()
                .completionConfig(CompletionConfig.toleratedFailureCount(1))
                .build();

        try (var parallel = context.parallel("tasks", config)) {
            parallel.branch("task-1", String.class,
                    ctx -> ctx.step("task-1", String.class, s -> "ok"));
            parallel.branch("task-2", String.class, ctx -> ctx.step("task-2", String.class, s -> {
                throw new RuntimeException("task 2 failed");
            }));
            parallel.branch("task-3", String.class,
                    ctx -> ctx.step("task-3", String.class, s -> "ok"));
            return parallel.get();
        }
    }
}

import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.CompletionConfig;
import software.amazon.lambda.durable.config.ParallelConfig;
import software.amazon.lambda.durable.model.ParallelResult;

public class CompletionConfigExample extends DurableHandler<Void, ParallelResult> {
    @Override
    public ParallelResult handleRequest(Void input, DurableContext context) {
        var config = ParallelConfig.builder()
                .completionConfig(CompletionConfig.firstSuccessful())
                .build();

        try (var parallel = context.parallel("race", config)) {
            parallel.branch("source-a", String.class,
                    ctx -> ctx.step("a", String.class, s -> "result from a"));
            parallel.branch("source-b", String.class,
                    ctx -> ctx.step("b", String.class, s -> "result from b"));
            parallel.branch("source-c", String.class,
                    ctx -> ctx.step("c", String.class, s -> "result from c"));
            return parallel.get();
        }
    }
}

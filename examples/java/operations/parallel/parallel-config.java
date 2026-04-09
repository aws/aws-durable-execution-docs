import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.CompletionConfig;
import software.amazon.lambda.durable.config.ParallelConfig;
import software.amazon.lambda.durable.model.ParallelResult;

public class ParallelConfigExample extends DurableHandler<Void, ParallelResult> {
    @Override
    public ParallelResult handleRequest(Void input, DurableContext context) {
        var config = ParallelConfig.builder()
                .maxConcurrency(2)
                .completionConfig(CompletionConfig.firstSuccessful())
                .build();

        try (var parallel = context.parallel("fetch-data", config)) {
            parallel.branch("primary", String.class,
                    ctx -> ctx.step("primary", String.class, s -> "primary result"));
            parallel.branch("secondary", String.class,
                    ctx -> ctx.step("secondary", String.class, s -> "secondary result"));
            parallel.branch("cache", String.class,
                    ctx -> ctx.step("cache", String.class, s -> "cache result"));
            return parallel.get();
        }
    }
}

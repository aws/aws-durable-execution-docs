import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.dag.DagCompletionConfig;
import software.amazon.lambda.durable.dag.DagConfig;
import software.amazon.lambda.durable.dag.DagResult;

public class DagConfigExample extends DurableHandler<Void, Integer> {
    @Override
    public Integer handleRequest(Void input, DurableContext context) {
        var config = DagConfig.builder()
                .maxConcurrency(2)
                .completionConfig(DagCompletionConfig.toleratedFailureCount(1))
                .build();

        DagResult result = context.dag(
                "sync-accounts",
                dag -> {
                    dag.step("account-a", String.class, (deps, ctx) -> "a");
                    dag.step("account-b", String.class, (deps, ctx) -> "b");
                    dag.step("account-c", String.class, (deps, ctx) -> "c");
                },
                config);

        return result.successCount();
    }
}

import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.otel.InvocationOtelPlugin;

public class InvocationOtelExample extends DurableHandler<Object, String> {
    @Override
    protected DurableConfig createConfiguration() {
        return DurableConfig.builder().withPlugins(new InvocationOtelPlugin()).build();
    }

    @Override
    protected String handleRequest(Object event, DurableContext context) {
        return context.step("process", String.class, ctx -> "done");
    }
}

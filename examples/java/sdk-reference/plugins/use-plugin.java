import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class ExampleHandler extends DurableHandler<Object, String> {
    @Override
    protected DurableConfig createConfiguration() {
        return DurableConfig.builder()
                .withPlugins(info -> new ExamplePlugin())
                .build();
    }

    @Override
    public String handleRequest(Object event, DurableContext context) {
        return context.step("process", String.class, stepCtx -> "done");
    }
}

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.otel.InvocationOtelPlugin;

public class InvocationOtelExample extends DurableHandler<Object, String> {
    @Override
    protected DurableConfig createConfiguration() {
        var plugin = new InvocationOtelPlugin(
                SdkTracerProvider.builder()
                        .addSpanProcessor(
                                SimpleSpanProcessor.create(OtlpGrpcSpanExporter.getDefault())));
        return DurableConfig.builder().withPlugins(plugin).build();
    }

    @Override
    protected String handleRequest(Object event, DurableContext context) {
        return context.step("process", String.class, ctx -> "done");
    }
}

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Executors;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.offload.filesystem.FileSystemPayloadOffloader;

public class DefaultFileSystemPayloadOffloader
        extends DurableHandler<Object, Map<String, String>> {
    @Override
    protected DurableConfig createConfiguration() {
        var offloader = FileSystemPayloadOffloader
            .builder(Path.of("/mnt/s3"))
            .build();

        return DurableConfig.builder()
            .withPayloadOffloader(offloader)
            .withPayloadOffloadExecutorService(Executors.newFixedThreadPool(4))
            .build();
    }

    @Override
    public Map<String, String> handleRequest(Object input, DurableContext context) {
        // Every supported operation result uses the configured offloader.
        return context.step(
            "fetch-order",
            Map.class,
            (StepContext ctx) -> Map.of("id", "order-123", "total", "99.99")
        );
    }
}

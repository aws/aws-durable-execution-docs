import java.nio.file.Path;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.offload.filesystem.FileSystemPayloadOffloader;

public class FileSystemPayloadOffloaderWalkthrough
        extends DurableHandler<Object, Map<String, String>> {
    @Override
    public Map<String, String> handleRequest(Object input, DurableContext context) {
        var offloader = FileSystemPayloadOffloader
            .builder(Path.of("/mnt/s3"))
            .build();

        StepConfig config = StepConfig.builder()
            .payloadOffloader(offloader)
            .build();

        // Only this step uses the filesystem offloader. Its SerDes is unchanged.
        return context.step(
            "fetch-order",
            Map.class,
            (StepContext ctx) -> Map.of("id", "order-123", "total", "99.99"),
            config
        );
    }
}

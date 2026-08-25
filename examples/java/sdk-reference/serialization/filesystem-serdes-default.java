import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Executors;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.serde.JacksonSerDes;
import software.amazon.lambda.durable.serde.filesystem.FileSystemSerDesStage;

public class DefaultFileSystemSerDes
        extends DurableHandler<Object, Map<String, String>> {
    @Override
    protected DurableConfig createConfiguration() {
        var fileSystemSerDes = new JacksonSerDes()
            .then(FileSystemSerDesStage.builder(Path.of("/mnt/s3")).build());

        return DurableConfig.builder()
            .withSerDes(fileSystemSerDes)
            .withSerDesExecutorService(Executors.newFixedThreadPool(4))
            .build();
    }

    @Override
    public Map<String, String> handleRequest(Object input, DurableContext context) {
        // Every operation result in this handler uses the configured pipeline.
        return context.step(
            "fetch-order",
            Map.class,
            (StepContext ctx) -> Map.of("id", "order-123", "total", "99.99")
        );
    }
}

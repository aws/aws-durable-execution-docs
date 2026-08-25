import java.nio.file.Path;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.serde.JacksonSerDes;
import software.amazon.lambda.durable.serde.SerDes;
import software.amazon.lambda.durable.serde.filesystem.FileSystemSerDesStage;

public class FileSystemSerDesWalkthrough
        extends DurableHandler<Object, Map<String, String>> {
    @Override
    public Map<String, String> handleRequest(Object input, DurableContext context) {
        SerDes fileSystemSerDes = new JacksonSerDes()
            .then(FileSystemSerDesStage.builder(Path.of("/mnt/s3")).build());

        StepConfig config = StepConfig.builder()
            .serDes(fileSystemSerDes)
            .build();

        // Pass the pipeline to one step. Other operations in this handler keep
        // using the default Jackson SerDes.
        return context.step(
            "fetch-order",
            Map.class,
            (StepContext ctx) -> Map.of("id", "order-123", "total", "99.99"),
            config
        );
    }
}

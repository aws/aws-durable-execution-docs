import java.nio.file.Path;
import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.serde.FileSystemSerDes;

public class FileSystemSerdesWalkthrough extends DurableHandler<Object, Integer> {
    private static final FileSystemSerDes FILES =
            FileSystemSerDes.builder(Path.of("/mnt/efs/durable-payloads")).build();

    @Override
    public Integer handleRequest(Object input, DurableContext context) {
        String report = context.step("build-report", String.class,
                step -> "report data".repeat(40000),
                StepConfig.builder().serDes(FILES).build());
        context.wait("cooldown", Duration.ofSeconds(1));
        return report.length();
    }
}

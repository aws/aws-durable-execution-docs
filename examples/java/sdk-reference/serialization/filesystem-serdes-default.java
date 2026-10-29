import java.nio.file.Path;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.serde.FileSystemSerDes;

public class FileSystemSerdesSharedConfig {
    private static final StepConfig STEP_CONFIG = StepConfig.builder()
            .serDes(FileSystemSerDes.builder(Path.of("/mnt/efs/durable-payloads")).build())
            .build();

    public static int buildReports(DurableContext context) {
        String first = context.step("first-report", String.class,
                step -> "first report".repeat(40000), STEP_CONFIG);
        String second = context.step("second-report", String.class,
                step -> "second report".repeat(40000), STEP_CONFIG);
        return first.length() + second.length();
    }
}

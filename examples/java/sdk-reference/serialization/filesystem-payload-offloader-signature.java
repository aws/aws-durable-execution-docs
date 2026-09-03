import java.nio.file.Path;
import software.amazon.lambda.durable.offload.PayloadOffloader;
import software.amazon.lambda.durable.offload.filesystem.FileSystemPayloadOffloader;

PayloadOffloader fileSystemOffloader = FileSystemPayloadOffloader
    .builder(Path.of("/mnt/s3"))
    .build();

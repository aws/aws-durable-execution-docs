import java.nio.file.Path;
import software.amazon.lambda.durable.offload.PayloadOffloader;
import software.amazon.lambda.durable.offload.filesystem.FileSystemPayloadOffloader;
import software.amazon.lambda.durable.offload.filesystem.PayloadOffloadMode;

// In OVERFLOW mode, small payloads stay inline in the checkpoint. The SDK
// writes to the filesystem when the serialized value approaches the limit.
PayloadOffloader overflowFileSystemOffloader = FileSystemPayloadOffloader
    .builder(Path.of("/mnt/s3"))
    .storageMode(PayloadOffloadMode.OVERFLOW)
    .build();

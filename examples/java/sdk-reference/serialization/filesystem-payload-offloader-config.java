import java.nio.file.Path;
import software.amazon.lambda.durable.offload.filesystem.FileSystemPathEncoding;
import software.amazon.lambda.durable.offload.filesystem.FileSystemPayloadOffloader;
import software.amazon.lambda.durable.offload.filesystem.PayloadOffloadMode;

FileSystemPayloadOffloader fileSystemOffloader = FileSystemPayloadOffloader
    .builder(Path.of("/mnt/s3"))
    .storageMode(PayloadOffloadMode.ALWAYS)
    .pathEncoding(FileSystemPathEncoding.URI)
    .build();

import java.nio.file.Path;
import software.amazon.lambda.durable.serde.JacksonSerDes;
import software.amazon.lambda.durable.serde.SerDes;
import software.amazon.lambda.durable.serde.filesystem.FileSystemSerDesStage;
import software.amazon.lambda.durable.serde.filesystem.FileSystemStorageMode;

// In OVERFLOW mode, small payloads stay inline in the checkpoint. The SDK
// writes to the filesystem when the serialized value approaches the limit.
SerDes overflowFileSystemSerDes = new JacksonSerDes().then(
    FileSystemSerDesStage.builder(Path.of("/mnt/s3"))
        .storageMode(FileSystemStorageMode.OVERFLOW)
        .build()
);

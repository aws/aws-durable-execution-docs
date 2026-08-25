import java.nio.file.Path;
import software.amazon.lambda.durable.serde.JacksonSerDes;
import software.amazon.lambda.durable.serde.SerDes;
import software.amazon.lambda.durable.serde.filesystem.FileSystemPathEncoding;
import software.amazon.lambda.durable.serde.filesystem.FileSystemSerDesStage;

// Hash the ARN and entity ID into fixed-length, filesystem-safe segments.
// Use HASH when entity IDs may contain unsafe characters or be very long.
SerDes hashedFileSystemSerDes = new JacksonSerDes().then(
    FileSystemSerDesStage.builder(Path.of("/mnt/s3"))
        .pathEncoding(FileSystemPathEncoding.HASH)
        .build()
);

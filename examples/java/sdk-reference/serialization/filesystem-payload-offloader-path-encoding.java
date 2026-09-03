import java.nio.file.Path;
import software.amazon.lambda.durable.offload.PayloadOffloader;
import software.amazon.lambda.durable.offload.filesystem.FileSystemPathEncoding;
import software.amazon.lambda.durable.offload.filesystem.FileSystemPayloadOffloader;

// Hash the ARN and entity ID into fixed-length, filesystem-safe segments.
// Use HASH when entity IDs may contain unsafe characters or be very long.
PayloadOffloader hashedFileSystemOffloader = FileSystemPayloadOffloader
    .builder(Path.of("/mnt/s3"))
    .pathEncoding(FileSystemPathEncoding.HASH)
    .build();

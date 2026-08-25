import java.nio.file.Path;
import software.amazon.lambda.durable.serde.filesystem.FileSystemPathEncoding;
import software.amazon.lambda.durable.serde.filesystem.FileSystemSerDesStage;
import software.amazon.lambda.durable.serde.filesystem.FileSystemStorageMode;

FileSystemSerDesStage fileSystemStage = FileSystemSerDesStage
    .builder(Path.of("/mnt/s3"))
    .storageMode(FileSystemStorageMode.ALWAYS)
    .pathEncoding(FileSystemPathEncoding.URI)
    .build();

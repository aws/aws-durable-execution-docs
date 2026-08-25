import java.nio.file.Path;
import software.amazon.lambda.durable.serde.JacksonSerDes;
import software.amazon.lambda.durable.serde.SerDes;
import software.amazon.lambda.durable.serde.filesystem.FileSystemSerDesStage;

FileSystemSerDesStage fileSystemStage = FileSystemSerDesStage
    .builder(Path.of("/mnt/s3"))
    .build();

SerDes fileSystemSerDes = new JacksonSerDes().then(fileSystemStage);

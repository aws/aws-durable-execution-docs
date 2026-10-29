import java.nio.file.Path;
import software.amazon.lambda.durable.serde.FileSystemSerDes;
import software.amazon.lambda.durable.serde.FileSystemStorageMode;
import software.amazon.lambda.durable.serde.FileSystemPathEncoding;
import software.amazon.lambda.durable.serde.JacksonSerDes;

public class FileSystemSerdesConfig {
    public static FileSystemSerDes create(Path basePath) {
        return FileSystemSerDes.builder(basePath)
                .storageMode(FileSystemStorageMode.ALWAYS)
                .pathEncoding(FileSystemPathEncoding.URI)
                .delegate(new JacksonSerDes())
                .build();
    }
}

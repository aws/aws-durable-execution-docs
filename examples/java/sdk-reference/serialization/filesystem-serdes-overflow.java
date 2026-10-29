import java.nio.file.Path;
import software.amazon.lambda.durable.serde.FileSystemSerDes;
import software.amazon.lambda.durable.serde.FileSystemStorageMode;

public class FileSystemSerdesOverflow {
    public static FileSystemSerDes create(Path basePath) {
        return FileSystemSerDes.builder(basePath)
                .storageMode(FileSystemStorageMode.OVERFLOW)
                .build();
    }
}

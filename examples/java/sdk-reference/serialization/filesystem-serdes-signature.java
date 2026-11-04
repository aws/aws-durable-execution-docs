import java.nio.file.Path;
import software.amazon.lambda.durable.serde.FileSystemSerDes;

public class FileSystemSerdesSignature {
    public static FileSystemSerDes create(Path basePath) {
        return FileSystemSerDes.builder(basePath).build();
    }
}

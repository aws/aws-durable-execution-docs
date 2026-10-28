import java.nio.file.Path;
import software.amazon.lambda.durable.serde.FileSystemSerDes;
import software.amazon.lambda.durable.serde.FileSystemPathEncoding;

public class FileSystemSerdesPathEncoding {
    public static FileSystemSerDes create(Path basePath) {
        return FileSystemSerDes.builder(basePath)
                .pathEncoding(FileSystemPathEncoding.HASH)
                .build();
    }
}

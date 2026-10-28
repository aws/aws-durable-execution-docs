import java.nio.file.Path;
import software.amazon.lambda.durable.serde.FileSystemSerDes;
import java.util.Map;

public class FileSystemSerdesPreview {
    public static FileSystemSerDes create(Path basePath) {
        return FileSystemSerDes.builder(basePath)
                .previewGenerator(value -> Map.of("characters", ((String) value).length()))
                .build();
    }
}

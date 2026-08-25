import java.nio.file.Path;
import software.amazon.lambda.durable.serde.JacksonSerDes;
import software.amazon.lambda.durable.serde.SerDes;
import software.amazon.lambda.durable.serde.filesystem.FileSystemSerDesStage;
import software.amazon.lambda.durable.serde.filesystem.PreviewConfig;
import software.amazon.lambda.durable.serde.filesystem.PreviewField;
import software.amazon.lambda.durable.serde.filesystem.PreviewMode;

PreviewConfig previewConfig = PreviewConfig.builder(PreviewMode.EXCLUDE_ALL)
    .include(
        PreviewField.anywhere("id"),
        PreviewField.anywhere("status")
    )
    .mask(PreviewField.anywhere("email"))
    .build();

// The checkpoint envelope contains the file pointer plus a small preview.
// The email field is visible but masked, so PII does not enter the checkpoint.
SerDes previewFileSystemSerDes = new JacksonSerDes().then(
    FileSystemSerDesStage.builder(Path.of("/mnt/s3"))
        .previewConfig(previewConfig)
        .build()
);

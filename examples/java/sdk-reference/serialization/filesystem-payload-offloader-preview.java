import java.nio.file.Path;
import software.amazon.lambda.durable.offload.PayloadOffloader;
import software.amazon.lambda.durable.offload.filesystem.FileSystemPayloadOffloader;
import software.amazon.lambda.durable.offload.filesystem.PreviewConfig;
import software.amazon.lambda.durable.offload.filesystem.PreviewField;
import software.amazon.lambda.durable.offload.filesystem.PreviewMode;

PreviewConfig previewConfig = PreviewConfig.builder(PreviewMode.EXCLUDE_ALL)
    .include(
        PreviewField.anywhere("id"),
        PreviewField.anywhere("status")
    )
    .mask(PreviewField.anywhere("email"))
    .build();

// The checkpoint envelope contains the file pointer plus a small preview.
// The email field is visible but masked, so PII does not enter the checkpoint.
PayloadOffloader previewFileSystemOffloader = FileSystemPayloadOffloader
    .builder(Path.of("/mnt/s3"))
    .previewConfig(previewConfig)
    .build();

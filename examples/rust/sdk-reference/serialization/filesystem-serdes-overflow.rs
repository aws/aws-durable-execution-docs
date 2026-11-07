use aws_durable_execution_sdk as durable;
use durable::serdes::{FileSystemSerdes, FileSystemSerdesConfig, FileSystemSerdesMode};

// Small payloads stay inline in the checkpoint. The SDK only writes to the
// filesystem when the checkpoint envelope would exceed the size limit.
fn overflow_file_system_serdes() -> FileSystemSerdes {
    FileSystemSerdes::with_config(
        "/mnt/s3",
        FileSystemSerdesConfig::builder()
            .storage_mode(FileSystemSerdesMode::Overflow)
            .build(),
    )
}

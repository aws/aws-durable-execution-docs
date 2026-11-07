use aws_durable_execution_sdk as durable;
use durable::serdes::{FileSystemPathEncoding, FileSystemSerdes, FileSystemSerdesConfig};

// Hash the execution ARN and the operation ID to keep directory and file names
// safe for the filesystem.
fn hashed_file_system_serdes() -> FileSystemSerdes {
    FileSystemSerdes::with_config(
        "/mnt/s3",
        FileSystemSerdesConfig::builder()
            .path_encoding(FileSystemPathEncoding::Hash)
            .build(),
    )
}

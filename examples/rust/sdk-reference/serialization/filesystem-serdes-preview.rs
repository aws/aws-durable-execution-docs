use aws_durable_execution_sdk as durable;
use durable::serdes::{
    FileSystemSerdes, FileSystemSerdesConfig, PreviewConfig, PreviewMode, build_preview,
};

// The checkpoint envelope stores the file pointer and a preview object. The
// console can show id and status without reading the full file. The checkpoint
// stores email only in masked form.
fn preview_file_system_serdes() -> FileSystemSerdes {
    let preview = PreviewConfig::builder(PreviewMode::ExcludeAll)
        .include("id")
        .include("status")
        .mask("email")
        .build();
    FileSystemSerdes::with_config(
        "/mnt/s3",
        FileSystemSerdesConfig::builder()
            .preview(move |json| Ok(build_preview(json, &preview)))
            .build(),
    )
}

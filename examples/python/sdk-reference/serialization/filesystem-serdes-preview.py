from aws_durable_execution_sdk_python.filesystem_serdes import (
    FileSystemSerDes,
    FileSystemSerDesConfig,
)
from aws_durable_execution_sdk_python.preview import (
    PreviewConfig,
    PreviewField,
    PreviewMode,
    build_preview,
)

# The checkpoint envelope stores the file pointer plus a small preview object,
# so the console can show id and status without reading the full file. The
# email field is visible but masked, so PII does not land in the checkpoint.
preview_fs_serdes = FileSystemSerDes(
    "/mnt/s3",
    FileSystemSerDesConfig(
        generate_preview=lambda value: build_preview(
            value,
            PreviewConfig(
                mode=PreviewMode.EXCLUDE_ALL,
                include=[PreviewField(name="id"), PreviewField(name="status")],
                mask=[PreviewField(name="email")],
            ),
        ),
    ),
)

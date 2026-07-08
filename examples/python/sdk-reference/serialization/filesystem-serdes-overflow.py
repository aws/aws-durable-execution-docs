from aws_durable_execution_sdk_python.filesystem_serdes import (
    FileSystemSerDes,
    FileSystemSerDesConfig,
    FileSystemSerDesMode,
)

# In OVERFLOW mode, small payloads stay inline in the checkpoint and no file
# is written. The SDK only writes to the filesystem when the serialized value
# exceeds the checkpoint size limit.
overflow_fs_serdes = FileSystemSerDes(
    "/mnt/s3",
    FileSystemSerDesConfig(storage_mode=FileSystemSerDesMode.OVERFLOW),
)

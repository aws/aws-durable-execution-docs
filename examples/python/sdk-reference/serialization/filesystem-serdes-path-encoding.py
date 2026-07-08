from aws_durable_execution_sdk_python.filesystem_serdes import (
    FileSystemSerDes,
    FileSystemSerDesConfig,
    FileSystemPathEncoding,
)

# Hash the ARN and the entity ID into fixed-length, filesystem-safe segments.
# Use HASH when entity IDs may contain unsafe characters or be very long.
hashed_fs_serdes = FileSystemSerDes(
    "/mnt/s3",
    FileSystemSerDesConfig(path_encoding=FileSystemPathEncoding.HASH),
)

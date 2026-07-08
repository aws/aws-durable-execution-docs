from aws_durable_execution_sdk_python.filesystem_serdes import (
    FileSystemSerDes,
    FileSystemSerDesConfig,
)
from aws_durable_execution_sdk_python.serdes import SerDes

fs_serdes: SerDes = FileSystemSerDes("/mnt/s3")

from aws_durable_execution_sdk_python.filesystem_serdes import (
    FileSystemSerDesConfig,
    FileSystemSerDesMode,
    FileSystemPathEncoding,
)

config = FileSystemSerDesConfig(
    storage_mode=FileSystemSerDesMode.ALWAYS,
    path_encoding=FileSystemPathEncoding.URI,
    generate_preview=None,
)

#[derive(Debug, Clone)]
#[non_exhaustive]
pub struct FileSystemSerdesConfig {
    pub storage_mode: FileSystemSerdesMode,
    pub path_encoding: FileSystemPathEncoding,
    pub overflow_threshold_bytes: usize,
    // ...
}

// ...

impl FileSystemSerdesConfigBuilder {
    pub fn storage_mode(mut self, mode: FileSystemSerdesMode) -> Self;
    // ...
    pub fn path_encoding(mut self, encoding: FileSystemPathEncoding) -> Self;
    // ...
    pub fn overflow_threshold_bytes(mut self, bytes: usize) -> Self;
    // ...
    pub fn preview<F>(mut self, f: F) -> Self
    where
        F: Fn(&str) -> Result<Option<String>, BoxError> + Send + Sync + 'static;
    // ...
    pub fn build(self) -> FileSystemSerdesConfig;
    // ...
}

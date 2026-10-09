type FileSystemSerdesConfig struct {
	// Mode controls when data is written to the filesystem. Default is
	// FileSystemSerdesModeAlways.
	Mode FileSystemSerdesMode

	// PathEncoding controls the directory and file names of offloaded
	// values. Default is FileSystemPathEncodingURI, the readable layout.
	PathEncoding FileSystemPathEncoding

	// GeneratePreview, when set, is called with each value that is written
	// to a file, and its non-nil result is stored in the checkpoint
	// envelope next to the file reference as the "preview" member.
	GeneratePreview func(value any) map[string]any
	// Has unexported fields.
}

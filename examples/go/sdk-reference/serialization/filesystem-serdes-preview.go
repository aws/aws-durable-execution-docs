package main

import (
	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

// The checkpoint envelope stores the file pointer plus a small preview object,
// so the console can show id and status without reading the full file. The
// email field is visible but masked, so PII does not land in the checkpoint.
var previewFileSystemSerdes = durable.NewFileSystemSerdes("/mnt/s3", durable.FileSystemSerdesConfig{
	GeneratePreview: func(value any) map[string]any {
		return durable.BuildPreview(value, durable.PreviewConfig{
			Mode:    durable.PreviewExcludeAll,
			Include: []durable.PreviewField{{Name: "id"}, {Name: "status"}},
			Mask:    []durable.PreviewField{{Name: "email"}},
		})
	},
})

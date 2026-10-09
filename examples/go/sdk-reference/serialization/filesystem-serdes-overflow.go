package main

import (
	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

// Small payloads stay inline in the checkpoint. The SDK only writes to the
// filesystem when the checkpoint envelope would exceed the size limit.
var overflowFileSystemSerdes = durable.NewFileSystemSerdes("/mnt/s3", durable.FileSystemSerdesConfig{
	Mode: durable.FileSystemSerdesModeOverflow,
})

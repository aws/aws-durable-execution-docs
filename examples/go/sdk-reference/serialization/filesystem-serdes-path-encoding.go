package main

import (
	"github.com/aws/aws-durable-execution-sdk-go/durable"
)

// Hash the ARN into a fixed-length, filesystem-safe directory name.
var hashedFileSystemSerdes = durable.NewFileSystemSerdes("/mnt/s3", durable.FileSystemSerdesConfig{
	PathEncoding: durable.FileSystemPathEncodingHash,
})

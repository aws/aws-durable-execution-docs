import {
  createFileSystemSerdes,
  FileSystemSerdesMode,
} from "@aws/durable-execution-sdk-js";

// Small payloads stay inline in the checkpoint. The SDK only writes to the
// filesystem when the checkpoint envelope would exceed the size limit.
export const overflowFileSystemSerdes = createFileSystemSerdes("/mnt/s3", {
  storageMode: FileSystemSerdesMode.OVERFLOW,
});

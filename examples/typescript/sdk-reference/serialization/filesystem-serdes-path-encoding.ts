import {
  createFileSystemSerdes,
  FileSystemPathEncoding,
} from "@aws/durable-execution-sdk-js";

// Hash the ARN and the entity ID into fixed-length, filesystem-safe segments.
// Use HASH when entity IDs may contain unsafe characters or be very long.
export const hashedFileSystemSerdes = createFileSystemSerdes("/mnt/s3", {
  pathEncoding: FileSystemPathEncoding.HASH,
});

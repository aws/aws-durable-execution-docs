import {
  FileSystemSerdesMode,
  FileSystemPathEncoding,
} from "@aws/durable-execution-sdk-js";

export interface FileSystemSerdesConfig {
  storageMode?: FileSystemSerdesMode;
  pathEncoding?: FileSystemPathEncoding;
  generatePreview?: (value: unknown) => Record<string, unknown> | undefined;
}

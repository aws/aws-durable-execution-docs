import {
  Serdes,
  FileSystemSerdesConfig,
} from "@aws/durable-execution-sdk-js";

export type CreateFileSystemSerdes = (
  basePath: string,
  config?: FileSystemSerdesConfig,
) => Serdes<unknown>;

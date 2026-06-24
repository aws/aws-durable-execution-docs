import {
  createFileSystemSerdes,
  buildPreview,
  PreviewMode,
} from "@aws/durable-execution-sdk-js";

// The checkpoint envelope stores the file pointer plus a small preview object,
// so the console can show id and status without reading the full file. The
// email field is visible but masked, so PII does not land in the checkpoint.
export const previewFileSystemSerdes = createFileSystemSerdes("/mnt/s3", {
  generatePreview: (value) =>
    buildPreview(value, {
      mode: PreviewMode.EXCLUDE_ALL,
      include: [{ name: "id" }, { name: "status" }],
      mask: [{ name: "email" }],
    }),
});

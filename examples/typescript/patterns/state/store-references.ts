// Wrong: full document returned and checkpointed.
const document = await context.step("fetch-document", async () => {
  return s3.getObject({ Bucket: "docs", Key: event.key });
});
await context.step("summarize", async () => summarize(document));

// Right: only the reference flows between steps.
const reference = await context.step("stage-document", async () => {
  const data = await s3.getObject({ Bucket: "docs", Key: event.key });
  const stagedKey = await stageForProcessing(data);
  return { bucket: "processing", key: stagedKey };
});

await context.step("summarize", async () => {
  const data = await s3.getObject(reference);
  return summarize(data);
});

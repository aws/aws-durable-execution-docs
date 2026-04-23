const results = await context.map(items, async (ctx, item) => {
  const outputKey = await ctx.step("process", async () => {
    const output = await processItem(item);
    return storeOutput(output); // returns an S3 key, not the output itself
  });
  return outputKey;
});

// `results` carries pointers, not payloads.

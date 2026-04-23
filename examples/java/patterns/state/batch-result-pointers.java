BatchResult<String> results = context.map(
    "process-items",
    items,
    String.class,
    (ctx, item) -> {
        Object output = processItem(item);
        return storeOutput(output); // returns an S3 key, not the output itself
    });

// `results` carries pointers, not payloads.

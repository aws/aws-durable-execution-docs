var resultKey = ctx.step("process_large_dataset", String.class, stepCtx -> {
    var data = downloadFromS3(input.get("s3_key"));
    var result = processData(data);
    return uploadToS3(result);  // Small checkpoint - just the S3 key
});
return Map.of("result_key", resultKey);

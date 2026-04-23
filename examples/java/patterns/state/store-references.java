record DocumentRef(String bucket, String key) {}

DocumentRef reference = context.step(
    "stage-document",
    DocumentRef.class,
    ctx -> {
        var data = s3.getObject(b -> b.bucket("docs").key(input.key()));
        String stagedKey = stageForProcessing(data);
        return new DocumentRef("processing", stagedKey);
    });

String summary = context.step(
    "summarize",
    String.class,
    ctx -> {
        var data = s3.getObject(b -> b.bucket(reference.bucket()).key(reference.key()));
        return makeSummary(data);
    });

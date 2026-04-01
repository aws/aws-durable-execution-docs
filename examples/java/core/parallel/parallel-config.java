// Configure to complete when first branch succeeds
ParallelConfig config = ParallelConfig.builder()
    .maxConcurrency(3)  // Run at most 3 branches concurrently
    .completionConfig(CompletionConfig.firstSuccessful())
    .build();

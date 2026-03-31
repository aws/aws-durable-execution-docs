ParallelConfig config = ParallelConfig.builder()
    .completionConfig(CompletionConfig.allSuccessful())
    .build();
// Stops executing new branches after first failure

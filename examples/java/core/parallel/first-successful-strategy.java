ParallelConfig config = ParallelConfig.builder()
    .completionConfig(CompletionConfig.firstSuccessful())
    .build();
// Ignores failures until at least one succeeds

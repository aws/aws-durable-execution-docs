// Require at least 3 successes, tolerate up to 2 failures
ParallelConfig config = ParallelConfig.builder()
    .completionConfig(CompletionConfig.builder()
        .minSuccessful(3)
        .toleratedFailureCount(2)
        .build())
    .build();

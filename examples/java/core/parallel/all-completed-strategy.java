ParallelConfig config = ParallelConfig.builder()
    .completionConfig(CompletionConfig.allCompleted())
    .build();
// If completion criteria are met early, remaining branches are marked STARTED

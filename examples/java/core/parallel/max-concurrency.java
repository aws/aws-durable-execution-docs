// Limit to 5 concurrent branches
ParallelConfig config = ParallelConfig.builder()
    .maxConcurrency(5)
    .build();

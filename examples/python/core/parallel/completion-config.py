# Require at least 3 successes, tolerate up to 2 failures
config = ParallelConfig(
    completion_config=CompletionConfig(
        min_successful=3,
        tolerated_failure_count=2,
    )
)

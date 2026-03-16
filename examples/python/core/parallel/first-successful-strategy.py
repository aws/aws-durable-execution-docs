config = ParallelConfig(
    completion_config=CompletionConfig.first_successful()
)
# Ignores failures until at least one succeeds

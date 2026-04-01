// Require at least 2 successes, tolerate up to 1 failure
ParallelConfig config = ParallelConfig.builder()
    .completionConfig(CompletionConfig.builder()
        .minSuccessful(2)
        .toleratedFailureCount(1)
        .build())
    .build();

var future1 = ctx.runInChildContextAsync("task-1", String.class,
    child -> child.step("work", String.class, stepCtx -> "Success 1"));

var future2 = ctx.runInChildContextAsync("task-2", String.class,
    child -> child.step("work", String.class, stepCtx -> "Success 2"));

var future3 = ctx.runInChildContextAsync("task-3", String.class,
    child -> child.step("work", String.class, stepCtx -> {
        throw new RuntimeException("This might fail");
    }));

DurableFuture.allOf(future1, future2, future3);

return Map.of(
    "status", "partial_success",
    "result1", future1.get(),
    "result2", future2.get());

var successFuture1 = ctx.runInChildContextAsync("task-success-1", String.class,
    child -> child.step("work", String.class, stepCtx -> "Success"));

var failingFuture = ctx.runInChildContextAsync("task-failing", String.class,
    child -> child.step("work", String.class, stepCtx -> {
        throw new RuntimeException("Task failed");
    }));

var successFuture2 = ctx.runInChildContextAsync("task-success-2", String.class,
    child -> child.step("work", String.class, stepCtx -> "Success"));

// Use allCompleted to collect per-branch status
DurableFuture.allOf(successFuture1, failingFuture, successFuture2);

return Map.of(
    "result1", successFuture1.get(),
    "result3", successFuture2.get());

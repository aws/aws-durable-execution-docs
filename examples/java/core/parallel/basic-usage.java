public class BasicParallelHandler extends DurableHandler<Map<String, Object>, List<String>> {

    @Override
    public List<String> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Define async branches to execute in parallel
        var future1 = ctx.runInChildContextAsync("task1", String.class,
            child -> child.step("task1", String.class, stepCtx -> "Task 1 complete"));

        var future2 = ctx.runInChildContextAsync("task2", String.class,
            child -> child.step("task2", String.class, stepCtx -> "Task 2 complete"));

        var future3 = ctx.runInChildContextAsync("task3", String.class,
            child -> child.step("task3", String.class, stepCtx -> "Task 3 complete"));

        // Execute all tasks concurrently
        DurableFuture.allOf(future1, future2, future3);

        return List.of(future1.get(), future2.get(), future3.get());
    }
}

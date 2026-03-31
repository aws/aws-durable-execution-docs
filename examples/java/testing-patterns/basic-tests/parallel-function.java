public class ParallelHandler extends DurableHandler<Map, List<String>> {
    @Override
    public List<String> handleRequest(Map input, DurableContext ctx) {
        String task1 = ctx.step("task1", String.class, stepCtx -> "Task 1 complete");
        String task2 = ctx.step("task2", String.class, stepCtx -> "Task 2 complete");
        String task3 = ctx.step("task3", String.class, stepCtx -> "Task 3 complete");

        return List.of(task1, task2, task3);
    }
}

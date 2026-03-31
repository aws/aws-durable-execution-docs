public class AccessResultsHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Launch three async child contexts
        var futureA = ctx.runInChildContextAsync("task-a", String.class,
            child -> child.step("step-a", String.class, stepCtx -> "Result A"));

        var futureB = ctx.runInChildContextAsync("task-b", String.class,
            child -> child.step("step-b", String.class, stepCtx -> "Result B"));

        var futureC = ctx.runInChildContextAsync("task-c", String.class,
            child -> child.step("step-c", String.class, stepCtx -> "Result C"));

        // Wait for all to complete
        DurableFuture.allOf(futureA, futureB, futureC);

        // Access results by index
        String first = futureA.get();   // "Result A"
        String second = futureB.get();  // "Result B"
        String third = futureC.get();   // "Result C"

        return Map.of(
            "first", first,
            "second", second,
            "third", third,
            "all", List.of(first, second, third)
        );
    }
}

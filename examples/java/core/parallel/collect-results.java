// Execute branches concurrently
var futureA = ctx.runInChildContextAsync("branch-a", String.class,
    child -> child.step("work", String.class, stepCtx -> "Result A"));
var futureB = ctx.runInChildContextAsync("branch-b", String.class,
    child -> child.step("work", String.class, stepCtx -> "Result B"));
var futureC = ctx.runInChildContextAsync("branch-c", String.class,
    child -> child.step("work", String.class, stepCtx -> "Result C"));

DurableFuture.allOf(futureA, futureB, futureC);

// Collect results
var resultA = futureA.get();
var resultB = futureB.get();
var resultC = futureC.get();

return Map.of(
    "results", List.of(resultA, resultB, resultC));

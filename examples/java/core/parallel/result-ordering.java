var future1 = ctx.runInChildContextAsync("first", String.class,
    child -> child.step("work", String.class, stepCtx -> "First"));

var future2 = ctx.runInChildContextAsync("second", String.class,
    child -> child.step("work", String.class, stepCtx -> "Second"));

var future3 = ctx.runInChildContextAsync("third", String.class,
    child -> child.step("work", String.class, stepCtx -> "Third"));

DurableFuture.allOf(future1, future2, future3);

// Results are in the same order as futures
var results = List.of(future1.get(), future2.get(), future3.get());
assert results.get(0).equals("First");
assert results.get(1).equals("Second");
assert results.get(2).equals("Third");

return Map.of("results", results);

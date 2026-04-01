// Outer parallel: process two groups concurrently
var groupAFuture = ctx.runInChildContextAsync("group-a", List.class, groupCtx -> {
    // Inner parallel for group A
    var a1 = groupCtx.runInChildContextAsync("a-item-1", String.class,
        child -> child.step("work", String.class, stepCtx -> "group-a-item-1"));
    var a2 = groupCtx.runInChildContextAsync("a-item-2", String.class,
        child -> child.step("work", String.class, stepCtx -> "group-a-item-2"));
    var a3 = groupCtx.runInChildContextAsync("a-item-3", String.class,
        child -> child.step("work", String.class, stepCtx -> "group-a-item-3"));

    DurableFuture.allOf(a1, a2, a3);
    return List.of(a1.get(), a2.get(), a3.get());
});

var groupBFuture = ctx.runInChildContextAsync("group-b", List.class, groupCtx -> {
    // Inner parallel for group B
    var b1 = groupCtx.runInChildContextAsync("b-item-1", String.class,
        child -> child.step("work", String.class, stepCtx -> "group-b-item-1"));
    var b2 = groupCtx.runInChildContextAsync("b-item-2", String.class,
        child -> child.step("work", String.class, stepCtx -> "group-b-item-2"));
    var b3 = groupCtx.runInChildContextAsync("b-item-3", String.class,
        child -> child.step("work", String.class, stepCtx -> "group-b-item-3"));

    DurableFuture.allOf(b1, b2, b3);
    return List.of(b1.get(), b2.get(), b3.get());
});

DurableFuture.allOf(groupAFuture, groupBFuture);

return Map.of(
    "groups_processed", 2,
    "results", List.of(groupAFuture.get(), groupBFuture.get()));

// In Java, child contexts are created inline via runInChildContext
var result = ctx.runInChildContext("my-context", Map.class, childCtx -> {
    // Your operations here
    return result;
});

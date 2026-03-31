// Use runInChildContext for nested operations
var result = ctx.runInChildContext("block1", String.class, childCtx -> {
    // Use child context for nested operations
    childCtx.wait("wait", Duration.ofSeconds(1));
    return childCtx.step("step2", String.class, stepCtx -> "nested step");
});
return Map.of("result", result);

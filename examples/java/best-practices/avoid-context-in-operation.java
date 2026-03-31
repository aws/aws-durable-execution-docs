// This will fail - context is being used inside its own step
var result = ctx.step("step1", String.class, stepCtx -> {
    // DON'T: Can't use context inside its own step operation
    ctx.wait("wait", Duration.ofSeconds(1));  // Error: using context inside step!
    return ctx.step("step2", String.class, s -> "nested");  // Error: nested ctx.step!
});
return Map.of("result", result);

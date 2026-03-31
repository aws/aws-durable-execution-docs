// Capture multiple arguments via lambda closure
var arg1 = "value";
var arg2 = 42;

var result = ctx.step("my-step", String.class, stepCtx -> {
    return arg1 + ": " + arg2;
});

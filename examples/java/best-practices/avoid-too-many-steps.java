var a = ctx.step("get_a", Integer.class, stepCtx -> (Integer) input.get("a"));
var b = ctx.step("get_b", Integer.class, stepCtx -> (Integer) input.get("b"));
var sum = ctx.step("sum", Integer.class, stepCtx -> a + b);
return Map.of("result", sum);

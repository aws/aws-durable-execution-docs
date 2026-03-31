var result = ctx.step("calculate_result", Integer.class, stepCtx -> {
    int a = (int) input.get("a");
    int b = (int) input.get("b");
    return a + b;
});
return Map.of("result", result);

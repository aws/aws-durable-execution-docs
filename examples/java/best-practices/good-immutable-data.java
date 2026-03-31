var data = Map.of("count", 0);
data = ctx.step("increment_count_1", Map.class, stepCtx -> incrementCount(data));
data = ctx.step("increment_count_2", Map.class, stepCtx -> incrementCount(data));
return data;

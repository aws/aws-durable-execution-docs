var result1 = ctx.invoke("invoke_1", "function-1", payload1, Map.class);
var result2 = ctx.invoke("invoke_2", "function-2", result1, Map.class);

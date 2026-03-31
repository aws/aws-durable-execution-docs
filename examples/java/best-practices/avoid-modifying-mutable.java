var data = new HashMap<String, Object>();
data.put("count", 0);
ctx.step("increment_count", Map.class, stepCtx -> incrementCount(data));
data.put("count", (int) data.get("count") + 1);  // DON'T: Mutation outside step
return data;

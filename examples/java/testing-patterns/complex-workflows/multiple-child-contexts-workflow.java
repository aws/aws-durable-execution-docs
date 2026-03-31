public class MultipleChildContextsHandler extends DurableHandler<Map, Map> {
    @Override
    public Map handleRequest(Map input, DurableContext ctx) {
        Map data = (Map) input.get("data");

        Map validated = ctx.runInChildContext("validation", Map.class, childCtx ->
            childCtx.step("validate", Map.class, stepCtx -> {
                var result = new HashMap<>(data);
                result.put("validated", true);
                return result;
            })
        );

        Map transformed = ctx.runInChildContext("transformation", Map.class, childCtx ->
            childCtx.step("transform", Map.class, stepCtx -> {
                var result = new HashMap<>(validated);
                result.put("transformed", true);
                return result;
            })
        );

        return transformed;
    }
}

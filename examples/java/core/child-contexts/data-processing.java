public class DataProcessingHandler extends DurableHandler<Map<String, Object>, Map> {

    @Override
    public Map handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Named child context
        var result = ctx.runInChildContext("data_processor", Map.class, childCtx -> {
            return childCtx.step("transform", Map.class,
                stepCtx -> transformData((Map) event.get("data")));
        });

        return result;
    }
}

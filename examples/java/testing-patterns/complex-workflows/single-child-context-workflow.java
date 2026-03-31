// Single child context workflow handler
public class SingleChildContextHandler extends DurableHandler<Map, Map> {
    @Override
    public Map handleRequest(Map input, DurableContext ctx) {
        String itemId = (String) input.get("item_id");

        Map result = ctx.runInChildContext("item_processing", Map.class, childCtx -> {
            childCtx.step("validate", String.class,
                stepCtx -> "Validating " + itemId);
            return childCtx.step("process", Map.class,
                stepCtx -> Map.of("item_id", itemId, "status", "processed"));
        });

        return result;
    }
}

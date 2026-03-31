public class RiskyOperationHandler extends DurableHandler<Map<String, Object>, Map> {

    @Override
    public Map handleRequest(Map<String, Object> event, DurableContext ctx) {
        Map data = (Map) event.get("data");

        var result = ctx.runInChildContext("risky_operation", Map.class, childCtx -> {
            try {
                var stepResult = childCtx.step("risky_step", Map.class,
                    stepCtx -> potentiallyFailingOperation(data));
                return Map.of("status", "success", "result", stepResult);
            } catch (Exception e) {
                // Handle error within child context
                var fallback = childCtx.step("fallback", Map.class,
                    stepCtx -> fallbackOperation(data));
                return Map.of("status", "fallback", "result", fallback, "error", e.getMessage());
            }
        });

        if ("fallback".equals(result.get("status"))) {
            return Map.of("warning", "Used fallback", "result", result.get("result"));
        }

        return result;
    }
}

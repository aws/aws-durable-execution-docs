public class ChildContextHandler extends DurableHandler<Map, String> {
    @Override
    public String handleRequest(Map input, DurableContext ctx) {
        int result = ctx.runInChildContext("child_operation", Integer.class, childCtx ->
            childCtx.step("multiply", Integer.class, stepCtx -> 5 * 2)
        );
        return "Child context result: " + result;
    }
}

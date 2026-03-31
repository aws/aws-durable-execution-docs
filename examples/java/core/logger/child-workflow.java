public class ChildWorkflowHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Top-level logger: includes durableExecutionArn
        ctx.getLogger().info("Starting workflow, event_id={}", event.get("id"));

        // Child context inherits logger and adds its own contextId/contextName
        String result = ctx.runInChildContext("child_workflow", String.class, childCtx -> {
            // Logger includes contextId for the child context
            childCtx.getLogger().info("Running in child context");

            String childResult = childCtx.step("child_step", String.class,
                stepCtx -> "child-processed"
            );

            childCtx.getLogger().info("Child workflow completed, result={}", childResult);
            return childResult;
        });

        ctx.getLogger().info("Workflow completed, result={}", result);
        return result;
    }
}

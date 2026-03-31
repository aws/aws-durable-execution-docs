// Java equivalent of @durable_execution decorator: extend DurableHandler
class MyWorkflowHandler extends DurableHandler<Map, Map> {

    @Override
    protected Map handleRequest(Map event, DurableContext ctx) {
        // Your durable function code
        return Map.of();
    }
}

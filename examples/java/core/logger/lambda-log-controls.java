public class LogControlHandler extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        ctx.getLogger().debug("This won't appear if log level is INFO or higher");
        ctx.getLogger().info("This will appear");

        String result = ctx.step("process_data", String.class, stepCtx -> "processed");

        return result;
    }
}

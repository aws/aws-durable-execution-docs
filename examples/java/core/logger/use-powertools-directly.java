// Java equivalent: using SLF4J directly (NOT replay-aware)

public class DirectSlf4jHandler extends DurableHandler<Map<String, Object>, String> {

    private static final Logger logger = LoggerFactory.getLogger(DirectSlf4jHandler.class);

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        logger.info("Starting workflow");

        String result = ctx.step("process_data", String.class, stepCtx -> "processed");

        logger.info("Workflow completed, result={}", result);
        return result;
    }
}

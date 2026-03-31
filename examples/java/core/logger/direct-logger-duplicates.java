public class DirectLoggerHandler extends DurableHandler<Map<String, Object>, String> {

    // Using SLF4J directly — NOT replay-aware
    private static final Logger logger = LoggerFactory.getLogger(DirectLoggerHandler.class);

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext ctx) {
        // This log appears on every replay
        logger.info("Starting workflow");

        String result1 = ctx.step("step_1", String.class, stepCtx -> "step1-done");
        // This log appears on every replay after step 1
        logger.info("Step 1 completed");

        String result2 = ctx.step("step_2", String.class, stepCtx -> "step2-done");
        // This log appears only once (no more replays after this)
        logger.info("Step 2 completed");

        return result1 + "-" + result2;
    }
}

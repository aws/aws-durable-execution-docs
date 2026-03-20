public class WaitBetweenStepsExample extends DurableHandler<Object, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Object input, DurableContext context) {
        // Start a process
        var jobId = context.step("start_job", String.class, stepCtx -> startJob());

        // Wait before checking status
        context.wait("initial_delay", Duration.ofSeconds(30));

        // Check status
        var status = context.step("check_status", String.class, stepCtx -> checkJobStatus(jobId));

        return Map.of("jobId", jobId, "status", status);
    }
}

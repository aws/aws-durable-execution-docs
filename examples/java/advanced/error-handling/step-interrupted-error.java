// This can happen if Lambda times out during step execution
try {
    var result = ctx.step("long_running", Map.class,
        stepCtx -> longRunningOperation());
    return result;
} catch (StepInterruptedException e) {
    // Step was interrupted, will retry on next invocation
    ctx.logger().warning("Step interrupted: " + e.getStepId());
    throw e;
}

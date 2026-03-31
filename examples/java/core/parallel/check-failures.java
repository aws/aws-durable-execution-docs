// Use map to run branches and check for failures
MapResult<String> result = ctx.map("parallel-work", items, String.class,
    (item, index, childCtx) -> childCtx.step("step-" + index, String.class, stepCtx -> process(item)));

if (!result.allSucceeded()) {
    return Map.of(
        "status", "partial_failure",
        "successful", result.succeeded(),
        "failed_count", result.failed().size()
    );
}

return Map.of(
    "status", "success",
    "results", result.succeeded()
);

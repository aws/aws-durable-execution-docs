var jobId = ctx.step("start_job", String.class, stepCtx -> startJob(input.get("data")));
ctx.wait("job_processing_wait", Duration.ofSeconds(30));  // Necessary
var result = ctx.step("check_job_status", Map.class, stepCtx -> checkJobStatus(jobId));
return result;

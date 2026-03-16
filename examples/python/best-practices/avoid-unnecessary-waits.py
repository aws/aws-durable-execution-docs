@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    job_id = context.step(start_job(event["data"]))
    context.wait(Duration.from_seconds(30), name="job_processing_wait")  # Necessary
    result = context.step(check_job_status(job_id))
    return result

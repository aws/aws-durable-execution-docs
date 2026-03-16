@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Start a process
    job_id = context.step(start_job())
    
    # Wait before checking status
    context.wait(duration=Duration.from_seconds(30), name="initial_delay")
    
    # Check status
    status = context.step(check_job_status(job_id))
    
    return {"job_id": job_id, "status": status}

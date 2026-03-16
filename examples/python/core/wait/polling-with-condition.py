from aws_durable_execution_sdk_python.waits import WaitForConditionConfig, FixedWait

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    job_id = context.step(start_job())
    
    # wait_for_condition handles the polling loop
    def check_status(state, check_context):
        status = get_job_status(state["job_id"])
        return {"job_id": state["job_id"], "status": status}
    
    result = context.wait_for_condition(
        check=check_status,
        config=WaitForConditionConfig(
            initial_state={"job_id": job_id},
            condition=lambda state: state["status"] == "completed",
            wait_strategy=FixedWait(Duration.from_minutes(1))
        )
    )
    return result

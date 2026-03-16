from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.waits import WaitForConditionConfig, ExponentialBackoff
from aws_durable_execution_sdk_python.config import Duration

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Poll until job completes
    def check_job_status(state, check_context):
        status = get_job_status(state["job_id"])
        return {
            "job_id": state["job_id"],
            "status": status,
            "done": status == "COMPLETED"
        }
    
    result = context.wait_for_condition(
        check=check_job_status,
        config=WaitForConditionConfig(
            initial_state={"job_id": "job-123", "done": False},
            condition=lambda state: state["done"],
            wait_strategy=ExponentialBackoff(
                initial_wait=Duration.from_seconds(5)
            )
        )
    )
    return result

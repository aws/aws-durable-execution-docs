from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.context import WaitForCallbackContext

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Wait for external approval
    def submit_for_approval(callback_id: str, ctx: WaitForCallbackContext):
        # Send callback_id to external approval system
        send_to_approval_system(callback_id)
    
    result = context.wait_for_callback(
        submitter=submit_for_approval,
        name="approval_wait"
    )
    return result

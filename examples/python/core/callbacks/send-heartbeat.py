# Send heartbeat for long-running operations
callback_id = "abc123-callback-id-from-durable-function"

lambda_client.send_durable_execution_callback_heartbeat(
    CallbackId=callback_id
)

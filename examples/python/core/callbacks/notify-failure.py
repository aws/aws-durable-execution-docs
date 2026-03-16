# When external system fails
callback_id = "abc123-callback-id-from-durable-function"

lambda_client.send_durable_execution_callback_failure(
    CallbackId=callback_id,
    Error={
        'ErrorType': 'PaymentDeclined',
        'ErrorMessage': 'Insufficient funds'
    }
)

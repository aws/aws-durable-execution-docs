# Message processor side (separate Lambda or service)
import boto3
import json

lambda_client = boto3.client('lambda')

def process_payment_message(event: dict):
    """Process payment and notify callback."""
    callback_id = event["callback_id"]
    amount = event["amount"]
    customer_id = event["customer_id"]
    
    try:
        # Process payment with external system
        result = payment_processor.charge(customer_id, amount)
        
        # Notify success
        result_data = json.dumps({
            'status': 'completed',
            'transaction_id': result.transaction_id,
        }).encode('utf-8')
        
        lambda_client.send_durable_execution_callback_success(
            CallbackId=callback_id,
            Result=result_data
        )
    except PaymentError as e:
        # Notify failure
        lambda_client.send_durable_execution_callback_failure(
            CallbackId=callback_id,
            Error={
                'ErrorType': 'PaymentError',
                'ErrorMessage': f'{e.error_code}: {str(e)}'
            }
        )

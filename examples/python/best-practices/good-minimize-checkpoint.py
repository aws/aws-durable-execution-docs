@durable_step
def process_large_dataset(step_context: StepContext, s3_key: str) -> str:
    data = download_from_s3(s3_key)
    result = process_data(data)
    result_key = upload_to_s3(result)
    return result_key  # Small checkpoint - just the S3 key

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    result_key = context.step(process_large_dataset(event["s3_key"]))
    return {"result_key": result_key}

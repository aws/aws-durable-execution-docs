@durable_step
def get_timestamp(step_context: StepContext) -> int:
    return int(time.time())

@durable_step
def generate_id(step_context: StepContext) -> str:
    return str(uuid.uuid4())

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    timestamp = context.step(get_timestamp())
    request_id = context.step(generate_id())
    return {"timestamp": timestamp, "request_id": request_id}

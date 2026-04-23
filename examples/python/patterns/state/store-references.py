# Wrong: full document returned and checkpointed.
@durable_step
def fetch_document(ctx: StepContext, key: str) -> dict:
    return s3.get_object(Bucket="docs", Key=key)


# Right: pass the reference, re-fetch in the next step.
@durable_step
def stage_document(ctx: StepContext, key: str) -> dict:
    data = s3.get_object(Bucket="docs", Key=key)
    staged_key = stage_for_processing(data)
    return {"bucket": "processing", "key": staged_key}


@durable_step
def summarize(ctx: StepContext, reference: dict) -> str:
    data = s3.get_object(Bucket=reference["bucket"], Key=reference["key"])
    return make_summary(data)


reference = context.step(stage_document(event["key"]))
summary = context.step(summarize(reference))

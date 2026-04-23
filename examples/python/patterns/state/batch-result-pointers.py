@durable_step
def process(ctx: StepContext, item: dict) -> str:
    output = process_item(item)
    return store_output(output)  # returns an S3 key, not the output itself


results = context.map(items, process)
# `results` carries pointers, not payloads.

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    context.step(lambda _: "Step 1 complete", name="step1")
    context.step(lambda _: "Step 2 complete", name="step2")
    context.step(
        lambda _: (_ for _ in ()).throw(RuntimeError("Step 3 failed")),
        name="step3"
    )
    return "Should not reach here"

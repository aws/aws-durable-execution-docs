# Wrong: replay may see different values of datetime.now().
from datetime import datetime

if datetime.now().hour < 12:
    context.step(run_morning(), name="morning-work")
else:
    context.step(run_afternoon(), name="afternoon-work")

# Right: the SDK checkpoints the decision.
shift = context.step(pick_shift(), name="pick-shift")
if shift == "morning":
    context.step(run_morning(), name="morning-work")
else:
    context.step(run_afternoon(), name="afternoon-work")


@durable_step
def pick_shift(ctx: StepContext) -> str:
    return "morning" if datetime.now().hour < 12 else "afternoon"

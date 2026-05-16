# WRONG
for name, comp_fn, resource_id in reversed(compensations):
    comp_fn(resource_id)  # if this throws error, subsequent compensations don't run

# CORRECT
for name, comp_step, resource_id in reversed(compensations):
    try:
        context.step(comp_step(resource_id), name=name)
    except Exception as e:
        context.logger.error(f"Compensation failed: {name}", e)
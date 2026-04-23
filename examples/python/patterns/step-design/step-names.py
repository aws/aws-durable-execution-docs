# Stable, descriptive name.
context.step(validate_order(order), name="validate-order")

# Dynamic but deterministic: include the item ID from the input.
context.step(save_item(item), name=f"save-item-{item['id']}")

# Wrong: non-deterministic name.
context.step(do_thing(), name=f"run-{time.time()}")

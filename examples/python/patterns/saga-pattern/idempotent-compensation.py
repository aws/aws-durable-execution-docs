try:
    reservation = context.step(reserve_inventory(event['order_id']))
    compensations.append((
        'cancel-reservation',
        cancel_reservation,
        reservation['id']  # reservation['id'] is the idempotency key and service uses it to deduplicate
    ))

    # ... more steps ...

except Exception as error:
    for name, comp_step, resource_id in reversed(compensations):
        try:
            context.step(comp_step(resource_id), name=name)
        except Exception as comp_error:
            context.logger.error(f"Compensation failed: {name}", comp_error)
    raise error
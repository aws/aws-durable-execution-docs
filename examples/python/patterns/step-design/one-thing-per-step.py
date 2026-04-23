# Right: each side effect gets its own step.
context.step(charge_payment(order), name="charge-payment")
context.step(send_confirmation(order), name="send-confirmation")
context.step(update_inventory(order), name="update-inventory")

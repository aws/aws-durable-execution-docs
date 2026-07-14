# WRONG - timestamp outside a step, changes on replay
import time
timestamp = time.time()
compensations.append(('cancel-reservation', lambda: cancel_reservation(timestamp)))  # timestamp changes on replay!

# CORRECT - data from step return value, stable on replay
reservation = context.step(reserve_inventory(order_id))
compensations.append(('cancel-reservation', lambda: cancel_reservation(reservation['id'])))
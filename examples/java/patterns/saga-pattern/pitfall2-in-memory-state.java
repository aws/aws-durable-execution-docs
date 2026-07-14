// WRONG - timestamp outside a step, changes on replay
long timestamp = System.currentTimeMillis();
compensations.add(new Compensation("cancel",
    () -> cancelWithTimestamp((String) reservation.get("id"), timestamp)
));

// CORRECT - data from step return value, stable on replay
Map<String, Object> reservation = context.step("reserve", Map.class,
    ctx -> reserveInventory(orderId));
compensations.add(new Compensation("cancel-reservation",
    () -> cancelReservation((String) reservation.get("id"))
));
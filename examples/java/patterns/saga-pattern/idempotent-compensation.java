try {
    Map<String, Object> reservation = context.step("reserve-inventory", Map.class,
        ctx -> reserveInventory(orderId));
    compensations.add(new Compensation(
        "cancel-reservation",
        // reservation id is the idempotency key and service uses it to deduplicate
        () -> cancelReservation((String) reservation.get("id"))
    ));

    // ... more steps ...

} catch (Exception error) {
    for (int i = compensations.size() - 1; i >= 0; i--) {
        Compensation comp = compensations.get(i);
        try {
            context.step(comp.name, Void.class, ctx -> { comp.fn.run(); return null; });
        } catch (Exception compError) {
            context.getLogger().error("Compensation failed: " + comp.name);
        }
    }
    throw error;
}
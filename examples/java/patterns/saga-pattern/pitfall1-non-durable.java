// WRONG
for (int i = compensations.size() - 1; i >= 0; i--) {
    compensations.get(i).fn.run(); // if this throws error, subsequent compensations don't run
}

// CORRECT
for (int i = compensations.size() - 1; i >= 0; i--) {
    Compensation comp = compensations.get(i);
    try {
        context.step(comp.name, Void.class, ctx -> { comp.fn.run(); return null; });
    } catch (Exception e) {
        context.getLogger().error("Compensation failed: " + comp.name);
    }
}
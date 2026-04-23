// Wrong: replay may see different values of LocalTime.now().
if (LocalTime.now().getHour() < 12) {
    context.step("morning-work", Void.class, ctx -> { runMorning(); return null; });
} else {
    context.step("afternoon-work", Void.class, ctx -> { runAfternoon(); return null; });
}

// Right: the SDK checkpoints the decision.
String shift = context.step(
    "pick-shift",
    String.class,
    ctx -> LocalTime.now().getHour() < 12 ? "morning" : "afternoon");
if ("morning".equals(shift)) {
    context.step("morning-work", Void.class, ctx -> { runMorning(); return null; });
} else {
    context.step("afternoon-work", Void.class, ctx -> { runAfternoon(); return null; });
}

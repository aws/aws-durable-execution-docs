// Wrong: calling context.step inside a step callback is invalid.
context.step("outer", Void.class, ctx -> {
    context.step("inner", Void.class, c -> { work(); return null; }); // ERROR
    return null;
});

// Right: group durable operations in a child context.
context.runInChildContext("order-pipeline", String.class, child -> {
    child.step("validate", Void.class, c -> { validate(); return null; });
    child.step("charge", Void.class, c -> { charge(); return null; });
    return "done";
});

// Stable, descriptive name.
context.step("validate-order", ValidationResult.class,
    ctx -> validateOrder(order));

// Dynamic but deterministic: include the item ID from the input.
context.step("save-item-" + item.id(), Void.class,
    ctx -> { saveItem(item); return null; });

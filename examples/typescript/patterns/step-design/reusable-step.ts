async function validateOrder(order: Order): Promise<ValidationResult> {
  // ...
}

await context.step("validate-order", () => validateOrder(order));

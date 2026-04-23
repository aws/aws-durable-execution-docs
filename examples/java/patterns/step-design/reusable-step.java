private ValidationResult validateOrder(Order order) {
    return new Validator().validate(order);
}

context.step("validate-order", ValidationResult.class,
    ctx -> validateOrder(order));

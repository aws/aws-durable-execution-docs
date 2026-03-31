// All steps use inline lambdas in Java
var result = ctx.step("validate-order", Map.class, stepCtx -> {
    return validateOrderLogic(orderId);
});

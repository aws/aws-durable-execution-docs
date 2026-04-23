String idempotencyKey = context.step(
    "idempotency-key",
    String.class,
    ctx -> UUID.randomUUID().toString());

Receipt receipt = context.step(
    "charge",
    Receipt.class,
    ctx -> paymentService.charge(
        input.amount(), input.cardToken(), idempotencyKey));

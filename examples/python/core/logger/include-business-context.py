context.logger.info(
    "Processing payment",
    extra={
        "order_id": order_id,
        "customer_id": customer_id,
        "payment_method": "credit_card"
    }
)

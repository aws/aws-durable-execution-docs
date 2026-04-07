import { DurableContext, durableExecution } from "aws-durable-execution-sdk-js";

interface OrderEvent {
  orderId: string;
  amount: number;
}

interface ValidationResult {
  valid: boolean;
  reason?: string;
}

interface PaymentResult {
  transactionId: string;
}

export const handler = durableExecution(
  async (event: OrderEvent, context: DurableContext) => {
    const validation = await context.invoke<OrderEvent, ValidationResult>(
      "validate-order",
      "validate-order-function:live",
      { orderId: event.orderId, amount: event.amount },
    );

    if (!validation.valid) {
      return { status: "rejected", reason: validation.reason };
    }

    const payment = await context.invoke<OrderEvent, PaymentResult>(
      "process-payment",
      "payment-processor-function:live",
      { orderId: event.orderId, amount: event.amount },
    );

    return { status: "completed", transactionId: payment.transactionId };
  },
);

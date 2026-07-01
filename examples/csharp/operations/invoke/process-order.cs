using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ProcessOrderExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, OrderResult>(Workflow, input, context);

    private async Task<OrderResult> Workflow(OrderEvent input, IDurableContext ctx)
    {
        ValidationResult validation = await ctx.InvokeAsync<OrderEvent, ValidationResult>(
            "validate-order-function:live",
            input,
            name: "validate-order");

        if (!validation.Valid)
        {
            return new OrderResult(Status: "rejected", Reason: validation.Reason, TransactionId: null);
        }

        PaymentResult payment = await ctx.InvokeAsync<OrderEvent, PaymentResult>(
            "payment-processor-function:live",
            input,
            name: "process-payment");

        return new OrderResult(Status: "completed", Reason: null, TransactionId: payment.TransactionId);
    }
}

public record OrderEvent(string OrderId, decimal Amount);
public record ValidationResult(bool Valid, string? Reason);
public record PaymentResult(string TransactionId);
public record OrderResult(string Status, string? Reason, string? TransactionId);

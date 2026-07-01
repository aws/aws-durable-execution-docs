using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class HandleInvocationErrorExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, OrderResult>(Workflow, input, context);

    private async Task<OrderResult> Workflow(OrderEvent input, IDurableContext ctx)
    {
        try
        {
            PaymentResult payment = await ctx.InvokeAsync<OrderEvent, PaymentResult>(
                "payment-processor-function:live",
                input,
                name: "process-payment");
            return new OrderResult(Status: "success", Reason: null, TransactionId: payment.TransactionId);
        }
        catch (InvokeTimedOutException)
        {
            return new OrderResult(Status: "failed", Reason: "payment timed out", TransactionId: null);
        }
        catch (InvokeFailedException e)
        {
            return new OrderResult(Status: "failed", Reason: e.Message, TransactionId: null);
        }
    }
}

public record OrderEvent(string OrderId);
public record PaymentResult(string TransactionId);
public record OrderResult(string Status, string? Reason, string? TransactionId);

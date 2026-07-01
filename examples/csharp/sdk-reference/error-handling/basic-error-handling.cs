using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class BasicErrorHandlingExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, OrderResult>(Workflow, input, context);

    private async Task<OrderResult> Workflow(OrderEvent input, IDurableContext ctx)
    {
        try
        {
            OrderResult result = await ctx.StepAsync(
                async (_, _) =>
                {
                    if (string.IsNullOrEmpty(input.OrderId))
                    {
                        throw new InvalidOperationException("orderId is required");
                    }
                    return new OrderResult(input.OrderId, Status: "processed");
                },
                name: "process-order");
            return result;
        }
        catch (StepException e)
        {
            // The step exhausted its retries; the SDK checkpointed the final
            // error and threw it here.
            ctx.Logger.LogError("Step failed: {Message}", e.Message);
            return new OrderResult(input.OrderId, Status: "error", Error: e.Message);
        }
    }
}

public record OrderEvent(string OrderId);
public record OrderResult(string OrderId, string Status, string? Error = null);

using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class CallbackTimeoutExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, Approval>(Workflow, input, context);

    private async Task<Approval> Workflow(OrderEvent input, IDurableContext ctx)
    {
        // Always set a timeout on a callback. Without one the execution waits up
        // to the execution timeout, holding the resource slot until an operator
        // intervenes.
        var config = new WaitForCallbackConfig
        {
            Timeout = TimeSpan.FromHours(24),
        };

        // The submitter hands the service-allocated callbackId to the external
        // system. The external system later calls the SDK's callback success or
        // failure endpoint with that id.
        Approval outcome = await ctx.WaitForCallbackAsync<Approval>(
            async (callbackId, _, ct) =>
                await ApprovalsService.RequestAsync(input.OrderId, callbackId, ct),
            name: "wait-for-approval",
            config: config);

        return outcome;
    }
}

public record OrderEvent(string OrderId);
public record Approval(bool Approved);

public static class ApprovalsService
{
    public static Task RequestAsync(string orderId, string callbackId, CancellationToken ct)
        => Task.CompletedTask;
}

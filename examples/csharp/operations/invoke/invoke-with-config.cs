using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class InvokeWithConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, OrderResult>(Workflow, input, context);

    private async Task<OrderResult> Workflow(OrderEvent input, IDurableContext ctx)
    {
        var config = new InvokeConfig
        {
            TenantId = input.TenantId,
        };

        OrderResult result = await ctx.InvokeAsync<OrderEvent, OrderResult>(
            "order-processor-function:live",
            input,
            name: "process-order",
            config: config);

        return result;
    }
}

public record OrderEvent(string OrderId, string TenantId);
public record OrderResult(string Status);

using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class WalkthroughExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, Order>(Workflow, input, context);

    private async Task<Order> Workflow(object input, IDurableContext ctx)
    {
        // No serializer configuration on the operation — the SDK serializes and
        // deserializes the result with the ILambdaSerializer registered on
        // ILambdaContext.Serializer.
        Order order = await ctx.StepAsync(
            async (_, _) => new Order("order-123", "99.99"),
            name: "fetch-order");
        return order;
    }
}

public record Order(string Id, string Total);

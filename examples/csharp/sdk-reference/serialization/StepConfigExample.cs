using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class StepConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, Order>(Workflow, input, context);

    private async Task<Order> Workflow(object input, IDurableContext ctx)
    {
        // StepConfig has no serializer slot. The step result is serialized with
        // the ILambdaSerializer registered on ILambdaContext.Serializer. To
        // customize serialization, register a custom ILambdaSerializer at the
        // host boundary instead of setting a per-step SerDes.
        var config = new StepConfig
        {
            RetryStrategy = RetryStrategy.Exponential(maxAttempts: 3),
        };

        Order order = await ctx.StepAsync(
            async (_, _) => new Order("order-123", "99.99"),
            name: "fetch-order",
            config: config);
        return order;
    }
}

public record Order(string Id, string Total);

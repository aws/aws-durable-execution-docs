using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.Serialization.SystemTextJson;

public class StepConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, Order>(Workflow, input, context);

    private async Task<Order> Workflow(object input, IDurableContext ctx)
    {
        // Set StepConfig.Serializer to serialize THIS step's result with a specific
        // ILambdaSerializer. When null (default), the step result is serialized with the
        // ILambdaSerializer registered on ILambdaContext.Serializer. Only this step is
        // affected — other operations and the handler's return value are unchanged.
        var config = new StepConfig
        {
            RetryStrategy = RetryStrategy.Exponential(maxAttempts: 3),
            Serializer = new CamelCaseLambdaJsonSerializer(),
        };

        Order order = await ctx.StepAsync(
            async (_, _) => new Order("order-123", "99.99"),
            name: "fetch-order",
            config: config);
        return order;
    }
}

public record Order(string Id, string Total);

using Amazon;
using Amazon.Lambda;
using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class CustomLambdaClientExample
{
    // Create a custom Lambda client with specific region and retry configuration.
    private static readonly IAmazonLambda CustomClient = new AmazonLambdaClient(
        new AmazonLambdaConfig
        {
            RegionEndpoint = RegionEndpoint.USWest2,
            MaxErrorRetry = 5,
            RetryMode = Amazon.Runtime.RequestRetryMode.Adaptive,
        });

    // Pass the custom client as the fourth argument to DurableFunction.WrapAsync.
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, Result>(Workflow, input, context, CustomClient);

    private Task<Result> Workflow(object input, IDurableContext ctx)
    {
        // Your durable function logic
        return Task.FromResult(new Result("success"));
    }

    public record Result(string Status);
}

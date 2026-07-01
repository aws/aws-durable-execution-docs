using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class AsyncWaitExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        // Start the wait and the step but don't await yet — WaitAsync returns a Task
        Task waitTask = ctx.WaitAsync(TimeSpan.FromSeconds(5), name: "min-delay");
        Task<string> stepTask = ctx.StepAsync(
            async (_, _) => ProcessData(input),
            name: "process");

        // Await both — guarantees at least 5 seconds elapsed
        await Task.WhenAll(waitTask, stepTask);

        return await stepTask;
    }

    private static string ProcessData(object input) => "processed";
}

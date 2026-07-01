using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ExecutionModelExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<FetchEvent, string>(Workflow, input, context);

    private async Task<string> Workflow(FetchEvent input, IDurableContext ctx)
    {
        // Step 1: Fetch data — result is checkpointed
        string data = await ctx.StepAsync(
            async (_, _) => FetchData(input.Id),
            name: "fetch-data");

        // Step 2: Wait 30 seconds without consuming compute resources
        await ctx.WaitAsync(TimeSpan.FromSeconds(30), name: "wait-30s");

        // Step 3: Process the data — only runs after the wait completes
        string result = await ctx.StepAsync(
            async (_, _) => ProcessData(data),
            name: "process-data");

        return result;
    }

    private static string FetchData(string id) => $"data-for-{id}";
    private static string ProcessData(string data) => $"processed-{data}";
}

public record FetchEvent(string Id);

using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ErrorHandlingExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, TaskSummary>(Workflow, input, context);

    private async Task<TaskSummary> Workflow(object input, IDurableContext ctx)
    {
        var config = new ParallelConfig
        {
            CompletionConfig = new CompletionConfig { ToleratedFailureCount = 1 },
        };

        IBatchResult<string> result = await ctx.ParallelAsync(
            new Func<IDurableContext, CancellationToken, Task<string>>[]
            {
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "ok", name: "task-1"),
                async (branch, ct) => await branch.StepAsync<string>(
                    (_, _) => throw new InvalidOperationException("task 2 failed"),
                    name: "task-2"),
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "ok", name: "task-3"),
            },
            name: "tasks",
            config: config);

        // BatchResult captures branch failures instead of throwing. Inspect
        // GetErrors() to handle them, or call ThrowIfError() to propagate.
        return new TaskSummary(
            Succeeded: result.SuccessCount,
            Failed: result.FailureCount,
            Results: result.GetResults(),
            Errors: result.GetErrors().Select(e => e.Message).ToList());
    }
}

public record TaskSummary(
    int Succeeded,
    int Failed,
    IReadOnlyList<string> Results,
    IReadOnlyList<string> Errors);

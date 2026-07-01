using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ConcurrentChildContextsExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, Results>(Workflow, input, context);

    // WRONG - a and b share the parent counter, so their IDs depend on which task
    // completes first. On replay, if the order differs, a gets b's checkpointed result
    // and b gets a's.
    private async Task<Results> WrongWorkflow(object input, IDurableContext ctx)
    {
        Task<string> a = ctx.StepAsync(async (_, _) => FetchA(), name: "fetch-a");
        Task<string> b = ctx.StepAsync(async (_, _) => FetchB(), name: "fetch-b");
        return new Results(await a, await b);
    }

    // CORRECT - each branch has its own isolated counter, so IDs are stable regardless
    // of completion order. Start both child contexts, then await them together.
    private async Task<Results> Workflow(object input, IDurableContext ctx)
    {
        Task<string> a = ctx.RunInChildContextAsync(
            async (child, _) => await child.StepAsync(async (_, _) => FetchA(), name: "fetch-a"),
            name: "branch-a");
        Task<string> b = ctx.RunInChildContextAsync(
            async (child, _) => await child.StepAsync(async (_, _) => FetchB(), name: "fetch-b"),
            name: "branch-b");
        return new Results(await a, await b);
    }

    private static string FetchA() => "result-a";
    private static string FetchB() => "result-b";
}

public record Results(string A, string B);

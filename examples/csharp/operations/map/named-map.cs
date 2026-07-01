using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class NamedMapExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<UserEvent, IReadOnlyList<string>>(
            Workflow, input, context);

    private async Task<IReadOnlyList<string>> Workflow(
        UserEvent input, IDurableContext ctx)
    {
        // The name is the optional trailing argument; omit it to leave the map unnamed
        IBatchResult<string> result = await ctx.MapAsync(
            input.UserIds,
            async (itemCtx, userId, index, userIds, ct) =>
                await itemCtx.StepAsync(
                    async (_, _) => $"processed-{userId}",
                    name: $"process-{index}"),
            name: "process-users");

        return result.GetResults();
    }
}

public record UserEvent(IReadOnlyList<string> UserIds);

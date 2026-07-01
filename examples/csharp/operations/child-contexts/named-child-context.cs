using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class NamedChildContextExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, string>(Workflow, input, context);

    private async Task<string> Workflow(OrderEvent input, IDurableContext ctx)
    {
        // Omit the name to infer one from the call site
        string unnamed = await ctx.RunInChildContextAsync(
            async (child, _) => await child.StepAsync(
                async (_, _) => $"{input.OrderId}:processed",
                name: "process"));

        // Pass a name as the name argument
        string named = await ctx.RunInChildContextAsync(
            async (child, _) => await child.StepAsync(
                async (_, _) => $"{input.OrderId}:processed",
                name: "process"),
            name: "process-order");

        return $"{unnamed} | {named}";
    }
}

public record OrderEvent(string OrderId);

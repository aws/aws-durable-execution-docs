using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class StepBoundaryExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        // Wrong: you cannot call another durable operation inside a step body.
        // The step body receives an IStepContext, not an IDurableContext, so there
        // is no StepAsync to call. Grouping durable operations belongs in a child
        // context instead.

        // Right: group durable operations in a child context.
        return await ctx.RunInChildContextAsync(
            async (child, ct) =>
            {
                await child.StepAsync(async (_, _) => Validate(), name: "validate");
                await child.StepAsync(async (_, _) => Charge(), name: "charge");
                return "done";
            },
            name: "order-pipeline");
    }

    private static bool Validate() => true;
    private static bool Charge() => true;
}

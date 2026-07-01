using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class MultiArgumentStepExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        string arg1 = "value";
        int arg2 = 42;

        // Capture arguments in the closure
        string result = await ctx.StepAsync(
            async (_, _) => MyStep(arg1, arg2),
            name: "my_step");
        return result;
    }

    private static string MyStep(string arg1, int arg2) => $"{arg1}: {arg2}";
}

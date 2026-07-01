using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class AddNumbersExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, int>(Workflow, input, context);

    private async Task<int> Workflow(object input, IDurableContext ctx)
    {
        int result = await ctx.StepAsync(
            async (_, _) => AddNumbers(5, 3),
            name: "add_numbers");
        return result;
    }

    private static int AddNumbers(int a, int b) => a + b;
}

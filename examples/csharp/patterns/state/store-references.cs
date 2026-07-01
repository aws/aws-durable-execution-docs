using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class StoreReferencesExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<DocumentEvent, string>(Workflow, input, context);

    private async Task<string> Workflow(DocumentEvent input, IDurableContext ctx)
    {
        // Wrong: full document returned and checkpointed.
        Document document = await ctx.StepAsync(
            async (_, ct) => await S3.GetObjectAsync("docs", input.Key, ct),
            name: "fetch-document");
        await ctx.StepAsync(
            async (_, _) => Summarize(document),
            name: "summarize-wrong");

        // Right: only the reference flows between steps.
        DocumentRef reference = await ctx.StepAsync(
            async (_, ct) =>
            {
                Document data = await S3.GetObjectAsync("docs", input.Key, ct);
                string stagedKey = StageForProcessing(data);
                return new DocumentRef("processing", stagedKey);
            },
            name: "stage-document");

        string summary = await ctx.StepAsync(
            async (_, ct) =>
            {
                Document data = await S3.GetObjectAsync(reference.Bucket, reference.Key, ct);
                return Summarize(data);
            },
            name: "summarize");

        return summary;
    }

    private static string Summarize(Document data) => $"summary-of-{data.Body}";
    private static string StageForProcessing(Document data) => $"staged/{data.Body}";
}

public record DocumentEvent(string Key);
public record Document(string Body);
public record DocumentRef(string Bucket, string Key);

internal static class S3
{
    public static Task<Document> GetObjectAsync(string bucket, string key, CancellationToken ct) =>
        Task.FromResult(new Document($"{bucket}/{key}"));
}

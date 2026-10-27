# Durable Execution SDK for .NET is generally available

_July 2026_

The AWS Lambda Durable Execution SDK for .NET is now generally available. .NET joins
TypeScript, Python, and Java as a fully supported language for building durable
functions.

## What this means

- Write durable workflows in C# with the same core operations available in the other
    SDKs: steps, waits, wait-for-condition, callbacks, invoke, and the `map` / `parallel`
    concurrency operations.
- Results are checkpointed and replayed, so a workflow can run for up to a year while
    surviving interruptions.
- The SDK integrates with the standard Lambda .NET tooling, including the
    `ILambdaSerializer` registered on the Lambda context for operation-result
    serialization.

## Get started

- [C# language guide](../sdk-reference/languages/csharp/index.md)
- [Quickstart](../getting-started/quickstart.md)

## Reference

- [AWS What's New: Durable Execution SDK for .NET is now generally available](https://aws.amazon.com/about-aws/whats-new/2026/07/lambdadf-dotnet/)

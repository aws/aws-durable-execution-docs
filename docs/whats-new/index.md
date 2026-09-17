# What's New

Announcements, releases, and notable changes for the AWS Lambda durable functions SDKs.

For the full per-release history, see each SDK's releases on GitHub:
[TypeScript](https://github.com/aws/aws-durable-execution-sdk-js/releases),
[Python](https://github.com/aws/aws-durable-execution-sdk-python/releases),
[Java](https://github.com/aws/aws-durable-execution-sdk-java/releases), and
.NET (in [aws-lambda-dotnet](https://github.com/aws/aws-lambda-dotnet/releases)).

## 2026

- **September 2026** [Durable Execution SDK for Python 2.x](2026-python-2.md)
    A breaking major release: typed per-operation errors, first-run serialize/deserialize
    round trip, and cross-SDK parity.
- **August 2026** [Durable Execution SDK for Rust is in preview](2026-08-rust-preview.md)
    An experimental preview of a Rust SDK for durable functions. Not for production use;
    feedback welcome through GitHub.
- **August 2026** [OpenTelemetry plugin for Java is now stable](2026-08-java-2.2-otel.md)
    The plugin platform and OpenTelemetry APIs are promoted from preview to stable in
    Java SDK 2.2.0, with dynamic plugin loading and expanded lifecycle hooks.
- **July 2026** [Durable Execution SDK for .NET is generally available](2026-07-dotnet-ga.md)
    The .NET SDK joins TypeScript, Python, and Java with full support for steps, waits,
    callbacks, and concurrency.
- **July 2026** [Bring your own Durable Execution SDK](2026-07-custom-sdk.md)
    Lambda durable functions now support a custom Durable Execution SDK, so you can build
    durable workflows against the checkpoint protocol directly.
- **June 2026** [Durable Execution SDK for Java 2.x](2026-06-java-2.md)
    A breaking major release: `semanticsPerRetry`, aligned log field names, per-context
    replay state, and first-run round trip by default.
- **April 2026** [Durable Execution SDK for Java is generally available](2026-04-java-ga.md)
    The Java SDK reaches general availability with steps, waits, callbacks, concurrency,
    and the testing library.

# Language Guides

Language-specific installation and setup for the AWS Durable Execution SDK.

- [TypeScript](typescript/index.md)
- [Python](python/index.md)
- [Java](java/index.md)
- [Go](go/index.md)
- [C#](csharp/index.md)

## Upgrade an existing application

Core 3 is released for TypeScript, Python, and Java. TypeScript and Python use OTel
2.0.0; Java's SDK and companion artifacts use 3.0.1. Follow the upgrade summary for
[TypeScript](typescript/index.md#upgrade-from-2x-to-30),
[Python](python/index.md#upgrade-from-2x-to-30), or
[Java](java/index.md#upgrade-from-2x-to-30). Each summary covers package versions, plugin
registration and behavior changes, and validation, and links to the SDK repository's full
migration guide.

For applications already on Java 3.0.0, see the
[3.0.1 patch upgrade](java/index.md#upgrade-from-300-to-301) for callback failure and OTel
error-reporting fixes.

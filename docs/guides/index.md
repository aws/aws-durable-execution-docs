# Guides

In-depth articles about individual aspects of durable functions. Where the SDK reference documents
each operation and the quickstarts get a first function running, a guide takes one subject and works
through it completely, including the trade-offs and the measurements behind a recommendation.

[JavaScript runtimes](javascript-runtimes.md) covers deploying a durable function on the managed
Node.js runtime and on container images carrying Bun, Deno, or LLRT. It works through the execution
role and the two timeouts every option needs, CDK for both zip and image packaging, the bundling
rules each runtime imposes, and measured cold start, memory, and replay figures for choosing between
them.

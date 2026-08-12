# JavaScript runtimes

AWS Lambda durable functions run on the managed Node.js runtimes and on container
images. Because the Durable Execution SDK talks to the service over the ordinary
Lambda data plane, it needs nothing from the runtime beyond normal handler
invocation. Lambda invokes your function with a JSON envelope, your function
returns an envelope, and the SDK checkpoints by calling
`CheckpointDurableExecution`. Any JavaScript runtime you can package into a
container image can therefore run a durable function.

This page covers four options and how to choose between them. Start with the
managed Node.js runtime. Reach for the others only when you have a specific
reason, because each one moves the runtime into code you maintain.

## The four options

### Managed Node.js

`nodejs22.x` and `nodejs24.x` are the JavaScript runtimes Lambda manages for
durable functions. AWS patches them and the Durable Execution SDK ships inside
them, though you should still bundle your own copy to control the version.

### Bun

Bun builds on JavaScriptCore and implements a broad subset of the Node API. The
Durable Execution SDK runs on it unmodified. Bun provides `AsyncLocalStorage`,
`util.formatWithOptions`, and everything else the SDK loads at startup, so no
functionality degrades. You supply a container image and a Runtime API loop.

### Deno

Deno builds on V8, adds a permissions model, and runs TypeScript directly. Like
Bun, it runs the Durable Execution SDK unmodified, and it used half the memory
Bun did in testing. It takes the most care to bundle for, because the CommonJS
interop banner described below is mandatory rather than optional.

### LLRT

[LLRT](https://github.com/awslabs/llrt) is an experimental runtime from AWS Labs.
It builds on QuickJS and compiles the AWS SDK into its binary. It started in
29 ms and used 31 MB of memory in testing, against 276 ms and 102 MB for the
managed Node.js runtime. It has no JIT compiler, so replay cost grows faster with
the number of operations than it does on the other runtimes, and it lacks
`AsyncLocalStorage`, which costs some log fidelity. Use it for small durable
functions that spend their time waiting on I/O. Avoid it for large `map`
fan-outs.

LLRT needs `@aws/durable-execution-sdk-js` 2.3.0 or later, which detects the two
Node APIs LLRT does not provide and falls back. On earlier versions, importing
the SDK on LLRT fails while the module loads.

## What every option needs

The execution role needs `lambda:CheckpointDurableExecution` and
`lambda:GetDurableExecutionState`. Attach the
[AWSLambdaBasicDurableExecutionRolePolicy](https://docs.aws.amazon.com/aws-managed-policy/latest/reference/AWSLambdaBasicDurableExecutionRolePolicy.html)
managed policy, which grants both alongside CloudWatch Logs access. See
[create the execution role](../../../getting-started/quickstart.md#create-the-execution-role)
for the full setup.

If you write the policy yourself, scope `Resource` to a qualified ARN pattern
such as `arn:aws:lambda:region:account:function:name:*`. A durable execution ARN
extends the function ARN with a version qualifier, so an unqualified ARN never
matches and Lambda denies the request.

Invoke durable functions against a version or an alias, never an unqualified
function name. Invoke asynchronously when the execution can outlast a single
invocation. Lambda rejects a synchronous invoke if `ExecutionTimeout` exceeds 15
minutes.

## Deploy with the AWS CDK

`aws-cdk-lib` supports durable functions through a `durableConfig` property on
`Function`, `NodejsFunction`, and `DockerImageFunction`:

```typescript
durableConfig: {
  executionTimeout: Duration.hours(1),   // required, 1 second to 366 days
  retentionPeriod: Duration.days(7),     // optional, 1 to 90 days, default 14
}
```

`executionTimeout` bounds the whole durable execution across every invocation
and wait. The function's own `timeout` bounds one invocation. Set both.

Each example below builds the same role:

```typescript
import * as iam from "aws-cdk-lib/aws-iam";

function durableRole(scope: Construct, id: string): iam.Role {
  return new iam.Role(scope, id, {
    assumedBy: new iam.ServicePrincipal("lambda.amazonaws.com"),
    managedPolicies: [
      iam.ManagedPolicy.fromAwsManagedPolicyName(
        "service-role/AWSLambdaBasicDurableExecutionRolePolicy",
      ),
    ],
  });
}
```

### Managed Node.js

```typescript
import { Duration } from "aws-cdk-lib";
import * as lambda from "aws-cdk-lib/aws-lambda";
import { NodejsFunction, OutputFormat } from "aws-cdk-lib/aws-lambda-nodejs";

new NodejsFunction(this, "OrderWorkflow", {
  entry: path.join(__dirname, "handler.ts"),
  handler: "handler",
  runtime: lambda.Runtime.NODEJS_22_X,
  architecture: lambda.Architecture.ARM_64,
  memorySize: 512,
  timeout: Duration.minutes(1),
  role: durableRole(this, "Role"),
  bundling: { format: OutputFormat.ESM, target: "node22", minify: true },
  durableConfig: {
    executionTimeout: Duration.hours(1),
    retentionPeriod: Duration.days(7),
  },
});
```

### Bun, Deno, or LLRT

The CDK is the same for all three. Only the Dockerfile changes.

```typescript
new lambda.DockerImageFunction(this, "OrderWorkflow", {
  code: lambda.DockerImageCode.fromImageAsset(path.join(__dirname, "image"), {
    file: "Dockerfile.bun", // or Dockerfile.deno, or Dockerfile.llrt
    platform: cdk.aws_ecr_assets.Platform.LINUX_ARM64,
  }),
  architecture: lambda.Architecture.ARM_64,
  memorySize: 512,
  timeout: Duration.minutes(1),
  role: durableRole(this, "Role"),
  durableConfig: {
    executionTimeout: Duration.hours(1),
    retentionPeriod: Duration.days(7),
  },
});
```

For the AWS CLI equivalent, see
[Quickstart for Container Image](../../../getting-started/quickstart-container-image.md).

### Older versions of the CDK

Versions of `aws-cdk-lib` that predate `durableConfig` have no support at either
the L1 or the L2 level. Override the property on the underlying `CfnFunction`
instead. The result is the same CloudFormation:

```typescript
const fn = new lambda.Function(this, "OrderWorkflow", {
  /* ... */
});

(fn.node.defaultChild as lambda.CfnFunction).addPropertyOverride("DurableConfig", {
  ExecutionTimeout: 3600,
  RetentionPeriodInDays: 7,
  // KMSKeyArn: "arn:aws:kms:..."  optional customer managed key
});
```

CloudFormation spells the key `KMSKeyArn`, not `KmsKeyArn`.

Two behaviors surprise people. Adding `durableConfig` to a function that already
exists replaces the resource, and the replacement fails if you also set
`functionName`, because the new function collides with the old name. Changing a
value inside `durableConfig` does not replace the resource. Deleting a durable
function waits for its running executions to finish, and CloudFormation gives up
after an hour. If a stack hangs on delete, look for executions still in flight.

## Build the container image

Neither Bun nor Deno implements the Lambda Runtime API, so each image needs a
loop that polls `/2018-06-01/runtime/invocation/next`, calls your handler, and
posts the result back. One file serves both, since it needs only `fetch` and
`process.env`. The loop needs nothing specific to durable functions. The LLRT
binary already contains one.

=== "Bun"

````
```dockerfile
FROM --platform=linux/arm64 oven/bun:1.3.14-slim
WORKDIR /var/task/
COPY handler.mjs bootstrap.mjs ./
CMD ["bun", "/var/task/bootstrap.mjs"]
```
````

=== "Deno"

````
```dockerfile
FROM --platform=linux/arm64 denoland/deno:2.9.5
WORKDIR /var/task/
COPY handler.mjs bootstrap.mjs ./
CMD ["deno", "run", "-A", "/var/task/bootstrap.mjs"]
```
````

=== "LLRT"

`````
Use the CLI build (`llrt-linux-arm64-full-sdk.zip`) rather than
`llrt-container-arm64-full-sdk`. Both run a Lambda function, but only the CLI
build can precompile the handler:

````
```dockerfile
FROM --platform=linux/arm64 busybox
WORKDIR /var/task/
COPY llrt /usr/bin/llrt
RUN chmod +x /usr/bin/llrt
COPY handler.mjs ./
RUN llrt compile handler.mjs handler.lrt && rm handler.mjs
ENV LAMBDA_HANDLER="handler.handler"
CMD ["llrt"]
```
````

Verify the LLRT binary against a published checksum before you copy it into
the image. Drop the `RUN llrt compile` line to ship plain JavaScript, which
also lets you use the container build.
`````

### Precompile the handler for LLRT

`llrt compile` parses the handler and generates bytecode at build time, then
compresses it. The runtime loads bytecode instead of parsing JavaScript, which
removed 9 ms from a 36 ms cold init in testing, around a quarter. It does not
change invocation duration, because QuickJS interprets the same bytecode either
way.

`LAMBDA_HANDLER` needs no change. LLRT resolves `.lrt` ahead of `.js`, `.mjs`,
and `.cjs`, so `handler.handler` finds `handler.lrt`.

Two constraints decide which binary compiles the handler. The container build
rejects the subcommand outright, reporting
`Not supported in "lambda" version.` And LLRT compresses bytecode with a zstd
dictionary that each build trains from its own embedded sources, so a file
compiled by one build fails to load in another with
`IO Error: Dictionary mismatch`. Compiling in the image with the same binary that
runs the function satisfies both, which is what the Dockerfile above does.

## Bundle the handler

| | Managed Node.js | Bun | Deno | LLRT |
|---|---|---|---|---|
| esbuild `platform` | `node` | `node` | `node` | `neutral` |
| `@aws-sdk` | bundle | bundle | bundle | leave external |
| CommonJS interop banner | not needed | advisable | required | not needed |
| Runtime API loop | provided | you write it | you write it | provided |

LLRT is the only runtime where you mark `@aws-sdk` external, because it compiles
`@aws-sdk/client-lambda` into its binary. That client is what the SDK loads to
checkpoint. Every other runtime needs it bundled.

Bundling the AWS SDK deserves attention, because the AWS SDK is CommonJS.
Compiling it into an ESM bundle leaves behind two constructs that an ESM module
does not provide, and Deno rejects both:

```console
Error: Dynamic require of "events" is not supported
ReferenceError: __filename is not defined
```

An esbuild banner reconstructs all three names from `import.meta.url`:

```typescript
banner: {
  js: [
    'import { createRequire as __createRequire } from "node:module";',
    'import { fileURLToPath as __fileURLToPath } from "node:url";',
    'import { dirname as __dirname_of } from "node:path";',
    "const require = __createRequire(import.meta.url);",
    "const __filename = __fileURLToPath(import.meta.url);",
    "const __dirname = __dirname_of(__filename);",
  ].join("\n"),
}
```

Add the banner even if you only target Bun. Bun defines `__filename` in ESM as a
Node compatibility convenience, so it hides the second error, and the same
bundle still fails on Deno and on Node. Neither problem appears when you run
unbundled from `node_modules`, so only a deployment surfaces them.

## What LLRT gives up

LLRT does not provide `AsyncLocalStorage`, and its `async_hooks` does not emit
`before` or `after` for promise resources, so the SDK cannot reconstruct one.
The SDK falls back to tracking the active operation only inside synchronous
code.

Checkpointing and replay behave identically, and the checkpoint data matches
byte for byte. Observability is what changes. Log records emitted after an
`await` lose `operationId`, `operationName`, and `attempt`. Replay-aware logging
can no longer suppress replayed records, so a log statement behind the replay
frontier repeats once per replay, which costs CloudWatch Logs on long
executions. The SDK also stops catching a parent or sibling context used inside
`runInChildContext`, unless the misuse happens synchronously.

That last check guards against a determinism bug, so enable the
`no-nested-durable-operations` rule from
`@aws/durable-execution-sdk-js-eslint-plugin`, which catches the same mistake at
build time.

## Measured behavior

Each runtime ran the same durable function on Lambda: arm64, 512 MB, in
`us-east-1`. The function ran steps, a step that failed once and retried, a
30 second wait, a child context, and a parallel block. Every runtime finished
across three invocations and returned identical results.

Each figure below is the median of three cold starts, each on a fresh function
version, after a discarded warm-up execution so that no measurement includes a
first-ever image pull.

| | Artifact | Cold init | First invocation | Warm invocation | Peak memory |
|---|---|---|---|---|---|
| Managed Node.js 22 | 0.2 MB zip | 276 ms | 714 ms | 180 ms | 102 MB |
| Bun | 202 MB image | 385 ms | 635 ms | 238 ms | 204 MB |
| Deno | 195 MB image | 462 ms | 875 ms | 258 ms | 118 MB |
| LLRT, bytecode | 17 MB image | 29 ms | 187 ms | 147 ms | 31 MB |

LLRT starts an order of magnitude faster than the managed Node.js runtime and
uses a third of the memory. It also wins both invocation columns here, which the
next section qualifies: this function performs ten operations, well inside the
range where LLRT leads.

Artifact size matters as much as the runtime. The Bun and Deno images are around
200 MB against LLRT's 17 MB, and their cold starts follow.

### Replay cost

A durable execution replays. Every time it resumes, your handler runs from the
top while completed operations return their checkpointed results. That work
scales with the number of operations, and a runtime without a JIT compiler pays
more for it. Running `context.map` over a growing item count, measuring CPU time
in process:

| Items | Operations | Node.js | LLRT |
|---|---|---|---|
| 25 | 54 | 34 ms | 26 ms |
| 250 | 504 | 56 ms | 55 ms |
| 1000 | 2004 | 100 ms | 163 ms |
| 2000 | 4004 | 188 ms | 381 ms |

LLRT leads below roughly 500 operations and falls behind above it, reaching
twice the CPU time at 4004 operations. Compute inside a step body widens the gap
further. LLRT wins on fixed costs instead, booting in 10 ms against 30 ms to
40 ms for Node.js, and a durable function pays those fixed costs on every
resume. Bun and Deno did not run this benchmark. Both compile with a JIT, so
this pattern should not apply to them.

## Choose a runtime

| | Managed Node.js | Bun | Deno | LLRT |
|---|---|---|---|---|
| Deployment | zip or container | container | container | container |
| SDK version | any | any | any | 2.3.0 or later |
| You maintain | nothing | bootstrap, image, patching | bootstrap, image, patching | image, patching |
| Replay performance | best | not measured | not measured | degrades past ~500 operations |
| Peak memory | 102 MB | 204 MB | 118 MB | 31 MB |
| Cold init | 276 ms | 385 ms | 462 ms | 29 ms |
| Log fidelity | full | full | full | degraded |
| `LocalDurableTestRunner` | works | works | works | needs `worker_threads` |
| Bundling effort | lowest | low | banner required | different externals |

Choose the managed Node.js runtime unless something specific rules it out. It
needs no bootstrap, no image, and no runtime patching, and it replays fastest as
operation counts grow.

Choose Bun or Deno when your team already runs them or wants their tooling. The
SDK needs no changes, the testing SDK works, and you take on a bootstrap, an
image, and runtime patching. Deno uses less memory. Bun takes less care to
bundle for.

Choose LLRT for small durable functions, in the low hundreds of operations, with
steps that wait on I/O. It starts an order of magnitude faster than the managed
runtime and uses a third of the memory, which matters because a durable execution
pays startup cost on every resume. Precompile the handler, accept the reduced log
fidelity, enable the ESLint rule, and measure again as your workflows grow, since
replay cost is where LLRT loses its lead.

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
interop banner and the builtin prefixes described below are both mandatory.

### LLRT

[LLRT](https://github.com/awslabs/llrt) is an experimental runtime from AWS Labs.
It builds on QuickJS and compiles the AWS SDK into its binary. It starts an order
of magnitude faster than the managed Node.js runtime and uses a third of the
memory, which matters because a durable execution pays startup cost on every
resume. It has no JIT compiler, so replay cost grows faster with the number of
operations than it does on the other runtimes, and it lacks `AsyncLocalStorage`,
which costs some log fidelity. Use it for small durable functions that spend
their time waiting on I/O. Avoid it for large `map` fan-outs.

LLRT needs `@aws/durable-execution-sdk-js` 2.3.0 or later, which detects the two
Node APIs LLRT does not provide and falls back. On earlier versions, importing
the SDK on LLRT fails while the module loads.

## What every option needs

The execution role needs `lambda:CheckpointDurableExecution` and
`lambda:GetDurableExecutionState`. Attach the
[AWSLambdaBasicDurableExecutionRolePolicy](https://docs.aws.amazon.com/aws-managed-policy/latest/reference/AWSLambdaBasicDurableExecutionRolePolicy.html)
managed policy, which grants both alongside CloudWatch Logs access. See
[create the execution role](../getting-started/quickstart.md#create-the-execution-role)
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

`aws-cdk-lib` 2.232.0 and later support durable functions through a
`durableConfig` property on `Function`, `NodejsFunction`, and
`DockerImageFunction`:

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
import { Construct } from "constructs";

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
import * as path from "node:path";
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
import * as path from "node:path";
import { Duration } from "aws-cdk-lib";
import { Platform } from "aws-cdk-lib/aws-ecr-assets";
import * as lambda from "aws-cdk-lib/aws-lambda";

new lambda.DockerImageFunction(this, "OrderWorkflow", {
  code: lambda.DockerImageCode.fromImageAsset(path.join(__dirname, "image"), {
    file: "Dockerfile.bun", // or Dockerfile.deno, or Dockerfile.llrt
    platform: Platform.LINUX_ARM64,
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
[Quickstart for Container Image](../getting-started/quickstart-container-image.md).

### Older versions of the CDK

`aws-cdk-lib` 2.231.1 and earlier have no support at either the L1 or the L2
level, because both landed together in 2.232.0. Override the property on the
underlying `CfnFunction` instead. The result is the same CloudFormation:

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

??? note "bootstrap.mjs, the Runtime API loop for Bun and Deno"

    ```javascript
    const BASE = `http://${process.env.AWS_LAMBDA_RUNTIME_API}/2018-06-01/runtime`;

    const post = (path, body) =>
      fetch(`${BASE}/${path}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body ?? null),
      });

    let handler;
    try {
      ({ handler } = await import("./handler.mjs"));
    } catch (error) {
      // Report an init failure, then exit so Lambda does not wait on a broken sandbox.
      await post("init/error", {
        errorType: error?.name ?? "InitError",
        errorMessage: String(error?.message ?? error),
      });
      process.exit(1);
    }

    while (true) {
      const next = await fetch(`${BASE}/invocation/next`);
      const requestId = next.headers.get("lambda-runtime-aws-request-id");
      const deadlineMs = Number(next.headers.get("lambda-runtime-deadline-ms") ?? 0);
      const event = await next.json();

      // The SDK reads awsRequestId and getRemainingTimeInMillis from the context.
      const context = {
        awsRequestId: requestId,
        invokedFunctionArn: next.headers.get("lambda-runtime-invoked-function-arn") ?? "",
        functionName: process.env.AWS_LAMBDA_FUNCTION_NAME ?? "",
        functionVersion: process.env.AWS_LAMBDA_FUNCTION_VERSION ?? "",
        getRemainingTimeInMillis: () => Math.max(0, deadlineMs - Date.now()),
      };

      try {
        const result = await handler(event, context);
        await post(`invocation/${requestId}/response`, result);
      } catch (error) {
        await post(`invocation/${requestId}/error`, {
          errorType: error?.name ?? "Error",
          errorMessage: String(error?.message ?? error),
          stackTrace: String(error?.stack ?? "").split("\n"),
        });
      }
    }
    ```

Place `bootstrap.mjs` and the bundled `handler.mjs` in the build context next to
the Dockerfile.

=== "Bun"

    ```dockerfile
    FROM --platform=linux/arm64 oven/bun:1.3.14-slim
    WORKDIR /var/task/
    COPY handler.mjs bootstrap.mjs ./
    CMD ["bun", "/var/task/bootstrap.mjs"]
    ```

=== "Deno"

    `-A` grants the network and environment access the loop needs.

    ```dockerfile
    FROM --platform=linux/arm64 denoland/deno:2.9.5
    WORKDIR /var/task/
    COPY handler.mjs bootstrap.mjs ./
    CMD ["deno", "run", "-A", "/var/task/bootstrap.mjs"]
    ```

=== "LLRT"

    Download `llrt-linux-arm64-full-sdk.zip` from the
    [LLRT releases](https://github.com/awslabs/llrt/releases), verify it against the
    published checksum, and unzip the `llrt` binary into the build context. Use this
    CLI build rather than `llrt-container-arm64-full-sdk`: both run a Lambda
    function, but only the CLI build can precompile the handler.

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

    LLRT supplies its own Runtime API loop, so this image needs no
    `bootstrap.mjs`. Drop the `RUN llrt compile` line to ship plain JavaScript,
    which also lets you use the container build.

### Precompile the handler for LLRT

`llrt compile` parses the handler and generates bytecode at build time, then
compresses it. The runtime loads bytecode instead of parsing JavaScript, which
removed about a quarter of cold init in testing. It does not change invocation
duration, because QuickJS interprets the same bytecode either way.

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

| Setting                 | Managed Node.js | Bun          | Deno         | LLRT           |
| ----------------------- | --------------- | ------------ | ------------ | -------------- |
| esbuild `platform`      | `node`          | `node`       | `node`       | `neutral`      |
| `@aws-sdk`              | bundle          | bundle       | bundle       | leave external |
| CommonJS interop banner | not needed      | advisable    | required     | not needed     |
| Runtime API loop        | provided        | you write it | you write it | provided       |

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

Deno is also strict about builtin specifiers. The SDK imports a few builtins
unprefixed, and Deno rejects those with `Import "crypto" not a dependency`,
asking for `node:crypto`. If you leave builtins external, rewrite the bare
specifiers as you bundle:

```typescript
const BUILTINS = ["crypto", "events", "util", "path", "url", "fs"];

const prefixBuiltins = {
  name: "prefix-node-builtins",
  setup(build) {
    const filter = new RegExp(`^(${BUILTINS.join("|")})$`);
    build.onResolve({ filter }, (args) => ({
      path: `node:${args.path}`,
      external: true,
    }));
  },
};
```

Node.js, Bun, and LLRT accept either form, so the prefixed one is safe
everywhere.

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

One testing note. The [local test runner](../testing/runner.md) starts a
checkpoint server in a `node:worker_threads` worker, which LLRT does not provide,
so the runner cannot execute on LLRT itself. This does not affect your test
suite: the runner runs on your development machine, so run it on Node.js while
deploying on LLRT.

## Measured behavior

Each runtime ran the same durable function on Lambda: steps, a step that failed
once and retried, a 30 second wait, a child context, and a parallel block. Every
runtime finished across three invocations and returned identical results, which
is the first thing to know: all four are functionally interchangeable here.

On startup, LLRT leads by an order of magnitude and uses the least memory, and
the managed Node.js runtime beats both container runtimes. Artifact size drives
much of that: the Bun and Deno images are around 200 MB against LLRT's 17 MB.

??? info "Measured 2026-08-12 on arm64, 512 MB, us-east-1"

    Median of three cold starts, each on a fresh function version, after a
    discarded warm-up execution so no measurement includes a first-ever image
    pull. Versions: `@aws/durable-execution-sdk-js` 2.3.0, `nodejs22.x`,
    `oven/bun:1.3.14-slim`, `denoland/deno:2.9.5`, LLRT `v0.8.1-beta` with a
    precompiled handler.

    | Runtime            | Artifact     | Cold init | First invocation | Warm invocation | Peak memory |
    | ------------------ | ------------ | --------- | ---------------- | --------------- | ----------- |
    | Managed Node.js 22 | 0.2 MB zip   | 276 ms    | 714 ms           | 180 ms          | 102 MB      |
    | Bun                | 202 MB image | 385 ms    | 635 ms           | 238 ms          | 204 MB      |
    | Deno               | 195 MB image | 462 ms    | 875 ms           | 258 ms          | 118 MB      |
    | LLRT, bytecode     | 17 MB image  | 29 ms     | 187 ms           | 147 ms          | 31 MB       |

    Replay CPU inside the handler, by operation count. A fixture maps over a
    growing number of items, then waits, which suspends the execution. The first
    invocation performs the map for real, and the invocation after the wait
    rebuilds state for every operation the map produced while doing no useful
    work, which isolates replay. Measured against an in-memory stand-in for the
    service, so no network time is included, and each runtime runs the artifact
    you would deploy. Median of five runs.

    | Operations | Node.js | Bun   | Deno   | LLRT   |
    | ---------- | ------- | ----- | ------ | ------ |
    | 54         | 33 ms   | 36 ms | 32 ms  | 26 ms  |
    | 504        | 53 ms   | 49 ms | 46 ms  | 54 ms  |
    | 2004       | 119 ms  | 75 ms | 86 ms  | 165 ms |
    | 2804       | 143 ms  | 92 ms | 107 ms | 243 ms |

    Isolating the replay invocation alone at 2804 operations: 5 ms on Node.js,
    4 ms on Bun, 5 ms on Deno, and 12 ms on LLRT.

### Replay cost

A durable execution is not one long invocation. When it suspends, at a wait, a
retry backoff, or a callback, Lambda ends the invocation. When it resumes, Lambda
invokes the handler again from the top. Your code runs from the beginning, and
the SDK intercepts every operation that already completed and returns its
checkpointed result instead of executing it again.

That catch-up work is replay, and it is not free. Each resume re-runs your
control flow, computes the identifier for every completed operation, finds it in
the restored execution state, and deserializes its result. The cost grows with
the number of completed operations, and a durable function pays it on every
resume, in billed duration. A workflow that suspends ten times pays it ten times.

Two things follow from the measurements above. Bun and Deno replay faster than
the managed Node.js runtime once histories grow, by roughly a third and a quarter.
And LLRT leads while histories are small, then falls behind, reaching about 1.7
times the CPU time of Node.js by 2800 operations. LLRT has no JIT compiler, and
replay is a hot, repetitive loop over history, which is what a JIT optimizes.
Compute inside a step body widens the gap further.

Reducing the number of operations helps every runtime and helps LLRT most.
Setting `nesting` to `NestingType.FLAT` on `map` and `parallel` skips the
per-iteration `CONTEXT` operation, which halved the operation count and cut
LLRT's replay CPU by nearly half in the same fixture.

## Choose a runtime

| Runtime                 | Managed Node.js  | Bun                        | Deno                        | LLRT                |
| ----------------------- | ---------------- | -------------------------- | --------------------------- | ------------------- |
| Deployment              | zip or container | container                  | container                   | container           |
| SDK version             | any              | any                        | any                         | 2.3.0 or later      |
| You maintain            | nothing          | bootstrap, image, patching | bootstrap, image, patching  | image, patching     |
| Startup                 | middle           | slower                     | slowest                     | fastest             |
| Memory                  | middle           | highest                    | middle                      | lowest              |
| Replay as history grows | baseline         | fastest                    | fast                        | slowest             |
| Log fidelity            | full             | full                       | full                        | degraded            |
| Bundling effort         | lowest           | low                        | banner and builtin prefixes | different externals |

Choose the managed Node.js runtime unless something specific rules it out. It
needs no bootstrap, no image, and no runtime patching. Bun and Deno replay
somewhat faster once histories grow large, which is rarely worth taking on a
runtime you maintain yourself.

Choose Bun or Deno when your team already runs them or wants their tooling. The
SDK needs no changes, the testing SDK works, and you take on a bootstrap, an
image, and runtime patching. Deno uses less memory. Bun takes less care to
bundle for.

Choose LLRT for small durable functions, in the low hundreds of operations, with
steps that wait on I/O, where its startup and memory advantages compound across
resumes. Precompile the handler, accept the reduced log fidelity, enable the
ESLint rule, and measure again as your workflows grow, since replay cost is where
LLRT loses its lead.

# SAM CLI

SAM CLI provides two ways to test durable functions:

1. local invocation inside a local container.
2. remote invocation against a deployed function.

## Local invoke

`sam local invoke` runs your function inside a local container that matches the Lambda
runtime. It handles checkpointing and replay automatically. Use it to catch issues the
in-process test runner cannot, such as packaging problems, environment variables, and
Lambda runtime configuration. No deployment is required.

Build your application, then invoke with a durable execution name:

```bash
sam build
sam local invoke MyDurableFunction --durable-execution-name my-test
```

SAM CLI drives the replay loop locally. After the function runs, inspect the execution
history:

```bash
sam local execution history <execution-id>
```

For functions that wait for an external callback, start the function in one terminal,
then resolve the callback from another:

```bash
# terminal 1
sam local invoke MyDurableFunction --durable-execution-name my-test

# terminal 2
sam local callback succeed <callback-id>
```

See the
[SAM CLI durable functions testing documentation](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/test-and-debug-durable-functions.html)
for full reference.

## Remote invoke

`sam remote invoke` invokes a deployed Lambda function in the cloud. Use it for quick
manual invocation and smoke testing after deployment, without writing test code.

```bash
sam remote invoke MyDurableFunction --stack-name my-stack --event '{"name": "world"}'
```

Inspect the execution history of a deployed execution:

```bash
sam remote execution history <execution-id>
```

For deployed functions that wait for a callback, resolve it from the CLI:

```bash
sam remote callback succeed <callback-id> --result '"approved"'
```

!!! note

    `sam remote invoke` is for manual invocation and smoke testing. For programmatic
    assertions against a deployed function, use the [Cloud Runner](cloud-runner.md) instead.

## See also

- [Runner](runner.md) Overview of all three runner options.
- [Authoring](authoring.md) Write tests with the local runner.
- [Cloud Runner](cloud-runner.md) Programmatic testing against a deployed function.

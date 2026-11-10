# Cloud Runner

The Durable Execution SDK Test Cloud Runner invokes a deployed Lambda function and polls
for completion. Use it to validate IAM permissions, real service integrations, and
deployment correctness. The same test assertions you write for local testing work with
the cloud runner.

## Set up the cloud runner

Create a cloud test runner with the qualified function name. The runner's Lambda client
must target the Region the function runs in. The runner invokes the function, extracts
the execution ARN from the response, and polls until the execution completes.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/cloud-runner/cloud-runner.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/cloud-runner/cloud-runner.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/cloud-runner/cloud-runner.java"
    ```

=== "Go"

    Pass `durabletest.NewCloudRunner` a Lambda client and the qualified function name.
    `config.LoadDefaultConfig` reads the Region and the credentials from the default
    chain, such as `AWS_REGION` and `~/.aws/config`. Save this as
    `cloud_runner_test.go`.

    ```go
    --8<-- "examples/go/testing/cloud-runner/cloud-runner.go"
    ```

=== "Rust"

    `durable::test_util::CloudRunner` is behind the SDK's `test-util` feature. Enable it
    on the dev-dependency as [Install the testing
    SDK](authoring.md#install-the-testing-sdk) shows. `CloudRunner::new` takes the
    function name or ARN, and `.qualifier(..)` sets the version, alias, or `$LATEST`,
    which is the default. `.region(..)` sets the Region of the runner's Lambda client.
    Without it the Region comes from the default chain, such as `AWS_REGION`. An
    execution that fails, times out, or stops returns `Ok` with that status. `run`
    returns `Err` when the runner itself fails, such as when a Lambda call fails on its
    last attempt (see [Configure polling](#configure-polling)). Save this as
    `tests/cloud_runner.rs`.

    ```rust
    --8<-- "examples/rust/testing/cloud-runner/cloud-runner.rs"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/testing/cloud-runner/cloud-runner.cs"
    ```

### Deploy before running

Cloud mode requires a deployed function. Deploy with your preferred tool before running
cloud tests:

```bash
sam build
sam deploy --stack-name my-durable-function
```

After deployment, get the qualified function name or ARN:

```bash
aws lambda get-function --function-name MyFunction \
  --query 'Configuration.FunctionArn' --output text
```

### Configure polling

The runner polls for completion, then retrieves the full operation history for
assertions. Configure the poll interval to control how frequently the runner checks for
completion.

=== "TypeScript"

    Pass `config: { pollInterval }` (in milliseconds) to the constructor:

    ```typescript
    --8<-- "examples/typescript/testing/cloud-runner/cloud-runner-timeout.ts"
    ```

=== "Python"

    Pass `timeout` in seconds to `run()`:

    ```python
    --8<-- "examples/python/testing/cloud-runner/cloud-runner-timeout.py"
    ```

=== "Java"

    Use `withTimeout(Duration)` to set the maximum wait time:

    ```java
    --8<-- "examples/java/testing/cloud-runner/cloud-runner-timeout.java"
    ```

=== "Go"

    Pass `durabletest.WithPollInterval` and `durabletest.WithTimeout` to
    `NewCloudRunner`. The defaults are 2 seconds and 5 minutes. When polling
    exceeds the timeout, `Run` returns an error. To bound a single call, pass
    `Run` a context with a deadline.

    ```go
    --8<-- "examples/go/testing/cloud-runner/cloud-runner-timeout.go"
    ```

=== "Rust"

    Pass `.poll_interval(Duration)` to the runner. The default is 1 second. The runner
    waits one interval before each `GetDurableExecution` status poll, and one interval
    before each history page that follows a page with a next marker. Each status poll
    and each history page request makes up to three attempts. The second attempt starts
    1000 ms after the first fails, and the third starts 1450 ms after the second fails.
    The error of the third attempt is the error of the call. `Invoke` and the calls that
    send a callback result or heartbeat make one attempt.

    `.max_poll_attempts(n)` bounds the status polls and the history reads of operation
    handle controls, such as `wait_for_data`, with one shared count. By default the
    count is unbounded. A poll counts once however many attempts it makes. A control's
    history read counts once however many pages and attempts it makes. The history read
    that builds the final result does not count. When the bound is reached first, `run`
    returns a result whose status is `ExecutionStatus::Running`. To bound how long the
    test waits, wrap `run` in `tokio::time::timeout`. Timing out that future does not
    stop the durable execution.

    ```rust
    --8<-- "examples/rust/testing/cloud-runner/cloud-runner-timeout.rs"
    ```

=== "C#"

    Set `DefaultTimeout` on `CloudTestRunnerOptions`, or pass a `timeout` to `RunAsync`:

    ```csharp
    --8<-- "examples/csharp/testing/cloud-runner/cloud-runner-timeout.cs"
    ```

### Required IAM permissions

Cloud mode requires AWS credentials in the environment. The runner uses the default
credential chain, so any standard AWS credential configuration works.

The IAM principal running the tests needs these permissions on the target function:

```json
{
  "Effect": "Allow",
  "Action": [
    "lambda:InvokeFunction",
    "lambda:GetDurableExecution",
    "lambda:GetDurableExecutionHistory"
  ],
  "Resource": "arn:aws:lambda:region:account-id:function:function-name:*"
}
```

## Troubleshooting

### Credentials not configured

The runner throws when it cannot find AWS credentials. Run `aws sts get-caller-identity`
to verify your credentials are configured.

### Function not found

Verify the function name or ARN is correct and the function exists in the target region:
`aws lambda get-function --function-name MyFunction`.

### Execution timeout

The function took longer than the configured timeout. Increase the timeout or check the
function logs: `aws logs tail /aws/lambda/MyFunction --follow`.

!!! note

    SAM CLI also supports invoking deployed functions with `sam remote invoke` and resolving
    callbacks with `sam remote callback succeed`. See [SAM CLI](sam-cli.md).

## See also

- [Authoring](authoring.md) Set up the local test runner.
- [Assertions](assertions.md) Inspect operation history after a test run.

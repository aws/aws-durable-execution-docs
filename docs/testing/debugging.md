# Remote Debugging

You can use [AWS Lambda Remote Debugging](https://docs.aws.amazon.com/lambda/latest/dg/debugging.html) with VS Code when testing deployed durable functions in the cloud.

## Overview

Because durable functions pin the specific version of the Lambda function during an execution, invocations in the same execution benefit from sandbox affinity on the Lambda placement service. As long as subsequent invocations occur within the 10–15 minute warm sandbox lifetime, invocations are routed to the same sandbox instance and can re-attach to the active debugger session.

## How it works

1. **Dedicated Debug Version:** When initiating a remote debugging session, Lambda publishes a unique, dedicated version of the function for the session.
2. **Dedicated Sandbox:** The Lambda placement service provisions a new sandbox instance dedicated to that specific version. The VS Code debugger attaches directly to this sandbox.
3. **Replay Re-attachment:** As your durable execution proceeds through steps and subsequent replay invocations are triggered, the placement service directs the execution to the same sandbox, allowing breakpoints to hit across replay cycles.
4. **Best-Effort Lifecycle:** Sandbox preservation relies on low concurrency during debugging, ensuring only the debugged execution targets that version instance.

## Caveats and limitations

- **60-Second Inactivity Timeout:** If the execution remains paused or frozen for more than 60 seconds (for example, during a long wait like `context.wait(300)`), the remote debugging session will disconnect, even though the underlying Lambda sandbox may stay warm.
- **Warm Sandbox Window:** If subsequent invocations occur after the 10–15 minute sandbox warm window has elapsed, the sandbox may be reclaimed, requiring a new debug session.

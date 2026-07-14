# Patterns

Best practices and design patterns for your Durable Execution SDK workflow code. For AWS
Lambda service concerns like function versioning and aliases, CloudWatch logs, and X-Ray
tracing, see the
[AWS Lambda durable functions best practices developer guide](https://docs.aws.amazon.com/lambda/latest/dg/durable-best-practices.html).

## Best practices

Best practices for deterministic workflows.

- [Determinism and replay](best-practices/determinism.md) How replay works, which code
    must stay deterministic, and how to move non-determinism into steps.
- [Idempotency and retries](best-practices/idempotency.md) When to use at-most-once
    versus at-least-once, idempotency tokens, and database patterns that tolerate
    retries.
- [Manage state](best-practices/state.md) Why checkpoint size matters, how to store
    references instead of payloads, and how to avoid size limits.
- [Step design](best-practices/step-design.md) Naming, granularity, decorators, error
    handling inside steps, and the boundary between orchestration and business logic.
- [Pause and resume](best-practices/pause-resume.md) Long waits, callbacks with
    timeouts, heartbeats, and polling external services with backoff.
- [Code organization](best-practices/code-organization.md) Separating business logic
    from orchestration, using child contexts, and grouping configuration.
- [Saga pattern](best-practices/saga-pattern.md) Implementing sagas with compensation logic
    for distributed transactions.
    
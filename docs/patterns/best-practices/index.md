# Best practices

Best practices for your Durable Execution SDK workflow code.

- [Determinism and replay](determinism.md) How replay works, which code must stay
    deterministic, and how to move non-determinism into steps.
- [Idempotency and retries](idempotency.md) When to use at-most-once versus
    at-least-once, idempotency tokens, and patterns that tolerate retries.
- [Manage state](state.md) Why checkpoint size matters, how to store references instead
    of payloads, and how to avoid size limits.
- [Step design](step-design.md) Naming, granularity, decorators, error handling inside
    steps, and the boundary between orchestration and business logic.
- [Pause and resume](pause-resume.md) Long waits, callbacks with timeouts, heartbeats,
    and polling external services with backoff.
- [Code organization](code-organization.md) Separating business logic from
    orchestration, using child contexts, and grouping configuration.

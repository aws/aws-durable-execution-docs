# SDK Reference

The SDK Reference covers everything you need to build, configure, and operate durable
functions.

## Operations

The core building blocks for constructing durable workflows:

- [Step](operations/step.md) Execute and checkpoint a unit of work
- [Wait](operations/wait.md) Pause execution for a duration
- [Wait for Condition](operations/wait-for-condition.md) Pause until an external
    condition is met
- [Callback](operations/callback.md) Resume execution via an external signal
- [Invoke](operations/invoke.md) Invoke another durable function
- [Parallel](operations/parallel.md) Execute multiple operations concurrently
- [Map](operations/map.md) Apply an operation across a collection
- [Child Context](operations/child-context.md) Scope a sub-workflow within a parent

## Error Handling

- [Errors](error-handling/errors.md) Exception types and error response formats
- [Retries](error-handling/retries.md) Configuring retry behavior for steps

## State

- [Serialization](state/serialization.md) How state is serialized between checkpoints

## Observability

- [Logging](observability/logging.md) Structured logging within durable functions

## Language Guides

Language-specific installation and configuration:

- [TypeScript](languages/typescript/index.md)
- [Python](languages/python/index.md)
- [Java](languages/java/index.md)

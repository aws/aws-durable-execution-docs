# Manage state

The result of a durable operation is recorded in a checkpoint. Checkpoints persist in
the durable execution backend. Large checkpoints consume durable execution storage that
count against the
[Durable execution storage written in megabytes](https://docs.aws.amazon.com/general/latest/gr/lambda-service.html),
and slow fetching and deserializing the operation history during replay. Keep
checkpointed values small. Fetch bulk data inside the step that needs it rather than
persist it into the checkpoint.

## What counts as durable state

Any data you pass as input to a handler, anything you return from a step or child
context, a wait-for-condition state object, or a wait-for-callback result gets
serialized and stored. Internal variables between steps do not persist across
invocations.

A variable local to the handler that holds the result of a step does not consume durable
state. That same variable, if it holds a million-row result set, becomes persistent
state once you return it from the step.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/state/durable-vs-local.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/state/durable-vs-local.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/state/durable-vs-local.java"
    ```

## Store references, not payloads

A common pattern is to do the fetch inside the step, extract the identifier, and return
the identifier from the step. The next step that needs the full payload does its own
fetch by ID. For very large payloads, stage the data in Amazon S3, DynamoDB, or another
store inside the first step and pass the key or version ID to the next step.

!!! warning

    Returning full API responses from a step puts the entire response in the checkpoint.
    Extract the IDs and fields you actually need. Drop the rest.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/state/store-references.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/state/store-references.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/state/store-references.java"
    ```

## Keep the handler input small

The handler input is stored once at the start of the execution and read from execution
state on every replay. A 2 MB event payload stays in execution state for the life of the
execution and is fetched and deserialized on every replay. For large inputs, publish the
payload to a store in the client, pass only the reference, and fetch inside the first
step.

!!! danger

    Large handler inputs sit in execution state for the life of the execution, count against
    the storage quota, and slow every replay. Move the payload to S3 or DynamoDB at the edge
    and pass only a key.

## Keep concurrency results small

The concurrency operations [`map`](../../sdk-reference/operations/map.md) and
[`parallel`](../../sdk-reference/operations/parallel.md) return a `BatchResult`
containing every item result. The SDK automatically handles large batch results for you.
When the full `BatchResult` exceeds 256 KB, the parent checkpoint stores a summary
(total count, success count, failure count) instead of the full payload, and
reconstructs the full result from per-item results of each child context's own
checkpoint on replay.

Even though this mechanism means the parent checkpoint does not consume excessive state,
the per-item results still persist in child checkpoints. A `map` over 1,000 items that
each return 5 KB stores 5 MB across the child checkpoints and consumes 1,000 operations
before retries. Plan both item count and size against
[AWS Lambda service quotas](https://docs.aws.amazon.com/general/latest/gr/lambda-service.html).

Keep per-item state small:

- **Return only what the next step needs.** Inside each per-item function, extract the
    identifier or summary and return it. The full per-item result stays out of the
    checkpoint entirely.
- **Stage raw results to S3 or DynamoDB.** Write each item's output to external storage
    from inside the per-item function, and return the key or version ID. The
    `BatchResult` then carries pointers, not payloads.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/state/batch-result-pointers.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/state/batch-result-pointers.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/state/batch-result-pointers.java"
    ```

!!! tip

    Treat the per-item result like a step return. If it is more than a few hundred bytes,
    stage the payload in S3 and return a key. Use the `FLAT` nesting type to skip per-item
    checkpointing when the work is cheap to re-run, lowering both operation count and
    storage.

## Custom serialization for heavy types

When the natural shape of your data is large or expensive to encode, configure a custom
serdes on the operation. The serdes can compress, encrypt, or offload to external
storage while returning only a pointer. See
[Serialization](../../sdk-reference/state/serialization.md) for details.

!!! tip

    A custom serdes can offload heavy payloads transparently. Steps still look like they
    return the object, while the serdes writes it to S3 and returns a pointer behind the
    scenes.

## See also

- [Serialization](../../sdk-reference/state/serialization.md) Per-operation serdes
    customization.
- [Map operation](../../sdk-reference/operations/map.md) and
    [parallel operation](../../sdk-reference/operations/parallel.md) for per-item result
    patterns.

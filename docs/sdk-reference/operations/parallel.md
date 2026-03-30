# Parallel Operations

## Table of Contents

- [What are parallel operations?](#what-are-parallel-operations)
- [Terminology](#terminology)
- [Key features](#key-features)
- [Getting started](#getting-started)
- [Method signature](#method-signature)
- [Basic usage](#basic-usage)
- [Collecting results](#collecting-results)
- [Configuration](#configuration)
- [Advanced patterns](#advanced-patterns)
- [Error handling](#error-handling)
- [Result ordering](#result-ordering)
- [Performance considerations](#performance-considerations)
- [Best practices](#best-practices)
- [FAQ](#faq)
- [Testing](#testing)
- [See also](#see-also)

[← Back to main index](../index.md)

## Terminology

**Parallel operation** - An operation that executes multiple functions concurrently using `context.parallel()`. Each function runs in its own child context.

**Branch** - An individual function within a parallel operation. Each branch executes independently and can succeed or fail without affecting other branches.

**BatchResult** - The result object returned by parallel operations. It includes a `BatchItem` for each branch plus counts and completion metadata.

**BatchItem** - A per-branch entry with `index`, `status`, `result`, and `error` (if failed).

**Completion strategy** - Configuration that determines when a parallel operation completes (e.g., all successful, first successful, all completed).

**Concurrent execution** - Multiple operations executing at the same time. The SDK manages concurrency automatically, executing branches in parallel.

**Child context** - An isolated execution context created for each branch. Each branch has its own step counter and operation tracking.

[↑ Back to top](#table-of-contents)

## What are parallel operations?

Parallel operations let you execute multiple functions concurrently within a durable function. Each function runs in its own child context and can perform steps, waits, or other operations independently. The SDK manages the concurrent execution and collects results automatically.

Use parallel operations to:
- Execute independent tasks concurrently for better performance
- Process multiple items that don't depend on each other
- Implement fan-out patterns where one input triggers multiple operations
- Reduce total execution time by running operations simultaneously

[↑ Back to top](#table-of-contents)

## Key features

- **Automatic concurrency** - Functions execute concurrently without manual thread management
- **Independent execution** - Each branch runs in its own child context with isolated state
- **Flexible completion** - Configure when the operation completes (all successful, first successful, etc.)
- **Error isolation** - One branch failing doesn't automatically fail others
- **Result collection** - Automatic collection of per-branch status, results, and errors
- **Concurrency control** - Limit maximum concurrent branches with `max_concurrency`
- **Checkpointing** - Results are checkpointed as branches complete

[↑ Back to top](#table-of-contents)

## Getting started

Here's a simple example of parallel operations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/simple-parallel.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/simple-parallel.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/simple-parallel.java"
    ```


When this function runs:
1. All three tasks execute concurrently
2. Each task runs in its own child context
3. Results are collected as tasks complete
4. The `BatchResult` contains per-branch status and results; `get_results()` returns successes

[↑ Back to top](#table-of-contents)

## Method signature

### context.parallel()

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/parallel-method-signature.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/parallel-method-signature.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/parallel-method-signature.java"
    ```


**Parameters:**

- `functions` - A sequence of callables that each receive a `DurableContext` and return a result. Each function executes in its own child context.
- `name` (optional) - A name for the parallel operation, useful for debugging and testing.
- `config` (optional) - A `ParallelConfig` object to configure concurrency limits, completion criteria, and serialization.

**Returns:** A `BatchResult[T]` object containing:
- `all` - List of `BatchItem` entries (one per branch) with `index`, `status`, `result`, and `error`
- `get_results()` - List of successful branch results
- `get_errors()` - List of `ErrorObject` entries for failed branches
- `succeeded()` / `failed()` / `started()` - `BatchItem` lists filtered by status
- `total_count`, `success_count`, `failure_count`, `started_count` - Branch counts by status
- `status` - Overall `BatchItemStatus` (FAILED if any branch failed)
- `completion_reason` - Why the operation completed
- `throw_if_error()` - Raises the first branch error, if any

**Raises:** Branch exceptions are captured in the `BatchResult`. Call `throw_if_error()` if you want to raise the first failure.

[↑ Back to top](#table-of-contents)

## Basic usage

### Simple parallel execution

Execute multiple independent operations concurrently:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/simple-parallel.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/simple-parallel.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/simple-parallel.java"
    ```


## Collecting results

The `BatchResult` object provides multiple ways to access results:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/collect-results.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/collect-results.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/collect-results.java"
    ```


Use `result.succeeded()`, `result.failed()`, or `result.started()` for `BatchItem` lists filtered by status, and `result.throw_if_error()` to raise the first failure when you want exceptions instead of error objects.

### Accessing individual results

Results are ordered by branch index:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/access-individual-results.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/access-individual-results.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/access-individual-results.java"
    ```


If you need branch-indexed access even when failures occur, iterate `result.all` and match on `item.index`.

[↑ Back to top](#table-of-contents)

## Configuration

Configure parallel behavior using `ParallelConfig`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/parallel-config.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/parallel-config.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/parallel-config.java"
    ```


### ParallelConfig parameters

**max_concurrency** - Maximum number of branches to execute concurrently. If `None` (default), all branches run concurrently. Use this to control resource usage:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/max-concurrency.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/max-concurrency.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/max-concurrency.java"
    ```


**completion_config** - Defines when the parallel operation completes:

- `CompletionConfig.all_successful()` - Requires all branches to succeed (default)
- `CompletionConfig.first_successful()` - Completes when any branch succeeds
- `CompletionConfig.all_completed()` - Completes when branches finish; check `started_count` if completion criteria are met early
- Custom configuration with specific success/failure thresholds

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/completion-config.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/completion-config.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/completion-config.java"
    ```


**serdes** - Custom serialization for the `BatchResult` object. If not provided, uses JSON serialization.

**item_serdes** - Custom serialization for individual branch results. If not provided, uses JSON serialization.

Note: If completion criteria are met early (min success reached or failure tolerance exceeded), unfinished branches are marked `STARTED` in `result.all` and counted in `started_count`.

[↑ Back to top](#table-of-contents)

## Advanced patterns

### First successful pattern

Execute multiple strategies and use the first one that succeeds:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/first-successful-pattern.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/first-successful-pattern.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/first-successful-pattern.java"
    ```


### Controlled concurrency

Limit concurrent execution to manage resource usage:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/controlled-concurrency.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/controlled-concurrency.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/controlled-concurrency.java"
    ```


### Partial success handling

Handle scenarios where some branches can fail:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/partial-success-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/partial-success-handling.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/partial-success-handling.java"
    ```


### Nested parallel operations

Parallel operations can contain other parallel operations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/nested-parallel.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/nested-parallel.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/nested-parallel.java"
    ```


[↑ Back to top](#table-of-contents)

## Error handling

Parallel operations handle errors gracefully, isolating failures to individual branches:

### Individual branch failures

When a branch fails, other branches continue executing:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/individual-branch-failures.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/individual-branch-failures.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/individual-branch-failures.java"
    ```


### Checking for failures

Inspect the `BatchResult` to detect and handle failures:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/check-failures.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/check-failures.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/check-failures.java"
    ```


### Completion strategies and errors

Different completion strategies handle errors differently:

**all_successful()** - Fails fast when any branch fails:
=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/all-successful-strategy.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/all-successful-strategy.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/all-successful-strategy.java"
    ```


**first_successful()** - Continues until one branch succeeds:
=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/first-successful-strategy.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/first-successful-strategy.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/first-successful-strategy.java"
    ```


**all_completed()** - Waits for branches to complete unless completion criteria are met early:
=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/all-completed-strategy.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/all-completed-strategy.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/all-completed-strategy.java"
    ```


[↑ Back to top](#table-of-contents)

## Result ordering

Results in `get_results()` maintain the same order as the input functions:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/result-ordering.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/result-ordering.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/result-ordering.java"
    ```


**Important:** Even though branches execute concurrently and may complete in any order, the SDK preserves the original order in the results list. This makes it easy to correlate results with inputs.

### Handling partial results

When some branches fail, `succeeded()` only contains results from successful branches, but the order is still preserved relative to the input:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/handle-partial-results.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/handle-partial-results.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/handle-partial-results.java"
    ```


[↑ Back to top](#table-of-contents)

## Performance considerations

### Concurrency limits

Use `max_concurrency` to balance performance and resource usage:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/concurrency-limits.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/concurrency-limits.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/concurrency-limits.java"
    ```


**When to limit concurrency:**
- Processing many items (hundreds or thousands)
- Calling external APIs with rate limits
- Managing memory usage with large data
- Controlling database connection pools

**When to use unlimited concurrency:**
- Small number of branches (< 50)
- Independent operations with no shared resources
- When maximum speed is critical

### Completion strategies

Choose the right completion strategy for your use case:

**first_successful()** - Best for:
- Redundant operations (multiple data sources)
- Racing multiple strategies
- Minimizing latency

**all_successful()** - Best for:
- Operations that must all succeed
- Fail-fast behavior
- Strict consistency requirements

**all_completed()** - Best for:
- Workflows where you want to observe branch outcomes end-to-end
- Collecting partial results (pair with tolerated failure settings if failures are expected)
- Logging or monitoring tasks

### Checkpointing overhead

Each branch creates checkpoints as it executes. For many small branches, consider:
- Batching items together
- Using map operations instead
- Grouping related operations

[↑ Back to top](#table-of-contents)

## Best practices

**Use parallel for independent operations** - Only parallelize operations that don't depend on each other's results.

**Limit concurrency for large workloads** - Use `max_concurrency` when processing many items to avoid overwhelming resources.

**Choose appropriate completion strategies** - Match the completion strategy to your business requirements (all must succeed vs. best effort).

**Handle partial failures gracefully** - Check `failure_count` and handle scenarios where some branches fail.

**Keep branches focused** - Each branch should be a cohesive unit of work. Don't make branches too granular.

**Use meaningful names** - Name your parallel operations for easier debugging and testing.

**Consider map operations for collections** - If you're processing a collection of similar items, use `context.map()` instead.

**Avoid shared state** - Each branch runs in its own context. Don't rely on shared variables or global state.

**Monitor resource usage** - Parallel operations can consume significant resources. Monitor memory and API rate limits.

**Test with realistic concurrency** - Test your parallel operations with realistic numbers of branches to catch resource issues.

[↑ Back to top](#table-of-contents)

## FAQ

**Q: What's the difference between parallel() and map()?**

A: `parallel()` executes a list of different functions, while `map()` executes the same function for each item in a collection. Use `parallel()` for heterogeneous operations and `map()` for homogeneous operations.

**Q: How many branches can I run in parallel?**

A: There's no hard limit, but consider resource constraints. For large numbers (> 100), use `max_concurrency` to limit concurrent execution.

**Q: Do branches execute in a specific order?**

A: Branches execute concurrently, so execution order is non-deterministic. However, results are returned in the same order as the input functions.

**Q: Can I use async functions in parallel operations?**

A: No, branch functions must be synchronous. If you need to call async code, use `asyncio.run()` inside your function.

**Q: What happens if all branches fail?**

A: The behavior depends on your completion configuration. You always get a `BatchResult`; inspect `get_errors()` or `failed()` to see failures, or call `throw_if_error()` to raise the first error.

**Q: Can I cancel running branches?**

A: Not directly. The SDK doesn't provide branch cancellation. Use completion strategies like `first_successful()` to stop starting new branches early.

**Q: How do I pass different arguments to each branch?**

A: Use lambda functions with default arguments:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/pass-different-arguments.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/pass-different-arguments.py"
    ```

=== "Java">

    ``` java
    --8<-- "examples/java/core/parallel/pass-different-arguments.java"
    ```


**Q: Can branches communicate with each other?**

A: No, branches are isolated. They can't share state or communicate during execution. Pass data through the parent context or use the results after parallel execution completes.

**Q: What's the overhead of parallel operations?**

A: Each branch creates a child context and checkpoints its results. For very small operations, the overhead might outweigh the benefits. Profile your specific use case.

**Q: Can I nest parallel operations?**

A: Yes, you can call `context.parallel()` inside a branch function. Each nested parallel operation creates its own set of child contexts.

[↑ Back to top](#table-of-contents)

## Testing

You can test parallel operations using the testing SDK. The test runner executes your function and lets you inspect branch results.

### Basic parallel testing

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/test-basic-parallel.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/test-basic-parallel.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/test-basic-parallel.java"
    ```


### Inspecting branch operations

Use the test result to inspect individual branch operations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/test-inspect-branches.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/test-inspect-branches.py"
    ```

=== "Python"

    ``` java
    --8<-- "examples/java/core/parallel/test-inspect-branches.java"
    ```


### Testing completion strategies

Test that completion strategies work correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/test-completion-strategies.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/test-completion-strategies.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/test-completion-strategies.java"
    ```


### Testing error handling

Test that parallel operations handle errors correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/test-error-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/test-error-handling.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/test-error-handling.java"
    ```


### Testing concurrency limits

Test that concurrency limits are respected:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/parallel/test-concurrency-limits.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/parallel/test-concurrency-limits.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/parallel/test-concurrency-limits.java"
    ```


For more testing patterns, see:
- [Basic tests](../testing-patterns/basic-tests.md) - Simple test examples
- [Complex workflows](../testing-patterns/complex-workflows.md) - Multi-step workflow testing
- [Best practices](../best-practices.md) - Testing recommendations

[↑ Back to top](#table-of-contents)

## See also

- [Map operations](map.md) - Process collections with the same function
- [Child contexts](child-contexts.md) - Understand child context isolation
- [Steps](steps.md) - Use steps within parallel branches
- [Error handling](../advanced/error-handling.md) - Handle errors in durable functions
- [Examples](https://github.com/awslabs/aws-durable-execution-sdk-python/tree/main/examples/src/parallel) - More parallel examples

[↑ Back to top](#table-of-contents)

[↑ Back to top](#table-of-contents)

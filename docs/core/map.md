# Map Operations

## Table of Contents

- [What are map operations?](#what-are-map-operations)
- [Terminology](#terminology)
- [Key features](#key-features)
- [Getting started](#getting-started)
- [Method signature](#method-signature)
- [Map function signature](#map-function-signature)
- [Configuration](#configuration)
- [Advanced patterns](#advanced-patterns)
- [Best practices](#best-practices)
- [Performance tips](#performance-tips)
- [FAQ](#faq)
- [Testing](#testing)
- [See also](#see-also)

[← Back to main index](../index.md)

## Terminology

**Map operation** - A durable operation that processes a collection of items in parallel, where each item is processed independently and checkpointed. Created using `context.map()`.

**Map function** - A function that processes a single item from the collection. Receives the context, item, index, and full collection as parameters.

**BatchResult** - The result type returned by map operations, containing results from all processed items with success/failure status.

**Concurrency control** - Limiting how many items process simultaneously using `max_concurrency` in `MapConfig`.

**Completion criteria** - Rules that determine when a map operation succeeds or fails based on individual item results.

[↑ Back to top](#table-of-contents)

## What are map operations?

Map operations let you process collections durably by applying a function to each item in parallel. Each item's processing is checkpointed independently, so if your function is interrupted, completed items don't need to be reprocessed.

Use map operations to:
- Transform collections with automatic checkpointing
- Process lists of items in parallel
- Handle large datasets with resilience
- Control concurrency behavior
- Define custom success/failure criteria

Map operations use `context.map()` to process collections efficiently. Each item becomes an independent operation that executes in parallel with other items.

[↑ Back to top](#table-of-contents)

## Key features

- **Parallel processing** - Items process concurrently by default
- **Independent checkpointing** - Each item's result is saved separately
- **Partial completion** - Completed items don't reprocess on replay
- **Concurrency control** - Limit simultaneous processing with `max_concurrency`
- **Flexible completion** - Define custom success/failure criteria
- **Result ordering** - Results maintain the same order as inputs

[↑ Back to top](#table-of-contents)

## Getting started

Here's a simple example of processing a collection:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/square.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/square.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/square.java"
    ```


When this function runs:
1. Each item is processed in parallel
2. The `square` function is called for each item
3. Each result is checkpointed independently
4. The function returns a dict with results `[1, 4, 9, 16, 25]`

If the function is interrupted after processing items 0-2, it resumes at item 3 without reprocessing the first three items.

[↑ Back to top](#table-of-contents)

## Method signature

### context.map()

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/map-method-signature.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/map-method-signature.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/map-method-signature.java"
    ```


**Parameters:**

- `inputs` - A sequence of items to process (list, tuple, or any sequence type).
- `func` - A callable that processes each item. See [Map function signature](#map-function-signature) for details.
- `name` (optional) - A name for the map operation, useful for debugging and testing.
- `config` (optional) - A `MapConfig` object to configure concurrency and completion criteria.

**Returns:** A `BatchResult[T]` containing the results from processing all items.

**Raises:** Exceptions based on the completion criteria defined in `MapConfig`.

[↑ Back to top](#table-of-contents)

## Map function signature

The map function receives four parameters:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/map-function-signature.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/map-function-signature.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/map-function-signature.java"
    ```


**Parameters:**

- `context` - A `DurableContext` for the item's processing. Use this to call steps, waits, or other operations.
- `item` - The current item being processed.
- `index` - The zero-based index of the item in the original collection.
- `items` - The full collection of items being processed.

**Returns:** The result of processing the item.

### Example

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/validate-email.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/validate-email.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/validate-email.java"
    ```


[↑ Back to top](#table-of-contents)

## Configuration

Configure map behavior using `MapConfig`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/process-item.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/process-item.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/process-item.java"
    ```


### MapConfig parameters

**max_concurrency** - Maximum number of items to process concurrently. If `None`, all items process in parallel. Use this to control resource usage.

**completion_config** - Defines when the map operation succeeds or fails:
- `CompletionConfig()` - Default, allows any number of failures
- `CompletionConfig.all_successful()` - Requires all items to succeed
- `CompletionConfig(min_successful=N)` - Requires at least N items to succeed
- `CompletionConfig(tolerated_failure_count=N)` - Fails after N failures
- `CompletionConfig(tolerated_failure_percentage=X)` - Fails if more than X% fail

**serdes** - Custom serialization for the entire `BatchResult`. If `None`, uses JSON serialization.

**item_serdes** - Custom serialization for individual item results. If `None`, uses JSON serialization.

**summary_generator** - Function to generate compact summaries for large results (>256KB).

[↑ Back to top](#table-of-contents)

## Advanced patterns

### Concurrency control

Limit how many items process simultaneously:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/fetch-data.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/fetch-data.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/fetch-data.java"
    ```

### Custom completion criteria

Define when the map operation should succeed or fail:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/custom-completion-criteria.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/custom-completion-criteria.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/custom-completion-criteria.java"
    ```

### Using context operations in map functions

Call steps, waits, or other operations inside map functions:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/fetch-user-data.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/fetch-user-data.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/fetch-user-data.java"
    ```


### Filtering and transforming results

Access individual results from the `BatchResult`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/check-inventory.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/check-inventory.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/check-inventory.java"
    ```


[↑ Back to top](#table-of-contents)

## Best practices

**Use descriptive names** - Name your map operations for easier debugging: `context.map(items, process_item, name="process_orders")`.

**Control concurrency for external calls** - When calling external APIs, use `max_concurrency` to avoid rate limits.

**Define completion criteria** - Use `CompletionConfig` to specify when the operation should succeed or fail.

**Keep map functions focused** - Each map function should process one item. Don't mix collection iteration with item processing.

**Use context operations** - Call steps, waits, or other operations inside map functions for complex processing.

**Handle errors gracefully** - Wrap error-prone code in try-except blocks or use completion criteria to tolerate failures.

**Consider collection size** - For very large collections (10,000+ items), consider processing in chunks.

**Monitor memory usage** - Large collections create many checkpoints. Monitor Lambda memory usage.

**Return only necessary data** - Large result objects increase checkpoint size. Return minimal data from map functions.

[↑ Back to top](#table-of-contents)

## Performance tips

**Parallel execution is automatic** - Items execute concurrently by default. Don't try to manually parallelize.

**Use max_concurrency wisely** - Too much concurrency can overwhelm external services or exhaust Lambda resources. Start conservative and increase as needed.

**Optimize map functions** - Keep map functions lightweight. Move heavy computation into steps within the map function.

**Use appropriate completion criteria** - Fail fast with `tolerated_failure_count` to avoid processing remaining items when many fail.

**Monitor checkpoint size** - Large result objects increase checkpoint size and Lambda memory usage. Return only necessary data.

**Consider memory limits** - Processing thousands of items creates many checkpoints. Monitor Lambda memory and adjust concurrency.

**Profile your workload** - Test with representative data to find optimal concurrency settings.

[↑ Back to top](#table-of-contents)

## FAQ

**Q: What's the difference between map and parallel operations?**

A: Map operations process a collection of similar items using the same function. Parallel operations execute different functions concurrently. Use map for collections, parallel for heterogeneous tasks.

**Q: How many items can I process?**

A: There's no hard limit, but consider Lambda's memory and timeout constraints. For very large collections (10,000+ items), consider processing in chunks.

**Q: Do items process in order?**

A: Items execute in parallel, so processing order is non-deterministic. However, results maintain the same order as inputs in the `BatchResult`.

**Q: What happens if one item fails?**

A: By default, the map operation continues processing other items. Use `CompletionConfig` to define failure behavior (e.g., fail after N failures).

**Q: Can I use async functions in map operations?**

A: No, map functions must be synchronous. If you need async processing, use `asyncio.run()` inside your map function.

**Q: How do I access individual results?**

A: The `BatchResult` contains a `results` list with each item's result:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/access-individual-results.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/access-individual-results.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/access-individual-results.java"
    ```


**Q: Can I nest map operations?**

A: Yes, you can call `context.map()` inside a map function to process nested collections.

**Q: What's the difference between serdes and item_serdes?**

A: `item_serdes` serializes individual item results as they complete. `serdes` serializes the entire `BatchResult` at the end. Use both for custom serialization at different levels.

**Q: How do I handle partial failures?**

A: Check the `BatchResult.results` list. Each result has a status indicating success or failure:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/handle-partial-failures.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/handle-partial-failures.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/test-handle-partial-failures.java"
    ```


**Q: Can I use map operations with steps?**

A: Yes, call `context.step()` inside your map function to execute steps for each item.

[↑ Back to top](#table-of-contents)

## Testing

You can test map operations using the testing SDK. The test runner executes your function and lets you inspect individual item results.

### Basic map testing

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/test-basic-map.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/test-basic-map.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/test-basic-map.java"
    ```


### Inspecting individual items

Use `result.get_map()` to inspect the map operation:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/test-inspect-items.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/test-inspect-items.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/test-inspect-items.java"
    ```


### Testing error handling

Test that individual item failures are handled correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/test-error-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/test-error-handling.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/test-error-handling.java"
    ```


### Testing with configuration

Test map operations with custom configuration:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/map/test-with-config.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/map/test-with-config.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/map/test-with-config.java"
    ```


For more testing patterns, see:
- [Basic tests](../testing-patterns/basic-tests.md) - Simple test examples
- [Complex workflows](../testing-patterns/complex-workflows.md) - Multi-step workflow testing
- [Best practices](../best-practices.md) - Testing recommendations

[↑ Back to top](#table-of-contents)

## See also

- [Parallel operations](parallel.md) - Execute different functions concurrently
- [Steps](steps.md) - Understanding step operations
- [Child contexts](child-contexts.md) - Organizing complex workflows
- [Examples](https://github.com/awslabs/aws-durable-execution-sdk-python/tree/main/examples/src/map) - More map examples

[↑ Back to top](#table-of-contents)

[↑ Back to top](#table-of-contents)

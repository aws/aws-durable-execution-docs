# Wait Operations

## Table of Contents

- [What are wait operations?](#what-are-wait-operations)
- [When to use wait operations](#when-to-use-wait-operations)
- [Terminology](#terminology)
- [Key features](#key-features)
- [Getting started](#getting-started)
- [Method signature](#method-signature)
- [Duration helpers](#duration-helpers)
- [Naming wait operations](#naming-wait-operations)
- [Multiple sequential waits](#multiple-sequential-waits)
- [Understanding scheduled_end_timestamp](#understanding-scheduled_end_timestamp)
- [Best practices](#best-practices)
- [FAQ](#faq)
- [Alternatives to wait operations](#alternatives-to-wait-operations)
- [Testing](#testing)
- [See also](#see-also)

[← Back to main index](../index.md)

## Terminology

**Wait operation** - A durable operation that pauses execution for a specified duration. Created using `context.wait()`.

**Duration** - A time period specified in seconds, minutes, hours, or days using the `Duration` class.

**Scheduled end timestamp** - The Unix timestamp (in milliseconds) when the wait operation is scheduled to complete.

**Suspend** - The process of pausing execution and saving state. The Lambda function exits and resumes later.

**Resume** - The process of continuing execution after a wait completes. The SDK automatically invokes your function again.

[↑ Back to top](#table-of-contents)

## What are wait operations?

Wait operations pause execution for a specified time. Your function suspends, the Lambda exits, and the system automatically resumes execution when the wait completes.

Unlike `time.sleep()`, waits don't consume Lambda execution time. Your function checkpoints, exits cleanly, and resumes later, even if the wait lasts hours or days.

[↑ Back to top](#table-of-contents)

## When to use wait operations

Use `context.wait()` when you need a simple time-based delay.

**Choose a different method if you need:**
- **Wait for external system response** → Use [`context.wait_for_callback()`](callbacks.md)
- **Wait until a condition is met** → Use `context.wait_for_condition()`
- **Wait for a step to complete** → Use [`context.step()`](steps.md)

[↑ Back to top](#table-of-contents)

## Key features

- **Durable pauses** - Execution suspends and resumes automatically
- **Flexible durations** - Specify time in seconds, minutes, hours, or days
- **Named operations** - Identify waits by name for debugging and testing
- **Automatic scheduling** - The SDK handles timing and resumption
- **Sequential waits** - Chain multiple waits together
- **No polling required** - The system invokes your function when ready

[↑ Back to top](#table-of-contents)

## Getting started

Here's a simple example of using a wait operation:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/wait-signature.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/wait-signature.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/wait-signature.java"
    ```


When this function runs:
1. The wait operation is checkpointed with a scheduled end time
2. The Lambda function exits (suspends)
3. After 5 seconds, the system automatically invokes your function again
4. Execution resumes after the wait and returns "Wait completed"

[↑ Back to top](#table-of-contents)

## Method signature

### context.wait()

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/basic-wait.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/basic-wait.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/basic-wait.java"
    ```


**Parameters:**

- `duration` (Duration, required) - How long to wait. Must be at least 1 second. Use `Duration.from_seconds()`, `Duration.from_minutes()`, `Duration.from_hours()`, or `Duration.from_days()` to create a duration.
- `name` (str, optional) - A name for the wait operation. Useful for debugging and testing.

**Returns:** None

**Raises:**
- `ValidationError` - If duration is less than 1 second

[↑ Back to top](#table-of-contents)

## Duration helpers

The `Duration` class provides convenient methods to specify time periods:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/duration-helpers.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/duration-helpers.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/duration-helpers.java"
    ```


If using duration in seconds, you can also create a Duration directly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/duration-from-seconds.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/duration-from-seconds.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/duration-from-seconds.java"
    ```


[↑ Back to top](#table-of-contents)

## Naming wait operations

You can name wait operations to make them easier to identify in logs and tests:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/named-wait.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/named-wait.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/named-wait.java"
    ```


Named waits are helpful when:
- You have multiple waits in your function
- You want to identify specific waits in test assertions
- You're debugging execution flow

[↑ Back to top](#table-of-contents)

## Understanding scheduled_end_timestamp

Each wait operation has a `scheduled_end_timestamp` attribute that indicates when the wait is scheduled to complete. This timestamp is in Unix milliseconds.

You can access this timestamp when inspecting operations in tests or logs. The SDK uses this timestamp to determine when to resume your function.

The scheduled end time is calculated when the wait operation is first checkpointed:
- Current time + wait duration = scheduled end timestamp

[↑ Back to top](#table-of-contents)

## Best practices

### Choose appropriate wait durations

When your function hits a wait, it terminates execution and doesn't incur compute charges during the wait period. The function resumes with a new invocation when the wait completes. Choose durations based on your workflow needs:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/appropriate-durations.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/appropriate-durations.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/appropriate-durations.java"
    ```


**Note:** If you have concurrent operations running (like parallel or map operations), those continue executing even when the main execution hits a wait. The function waits for all concurrent operations to complete before terminating.

### Use named waits for clarity

Name your waits when you have multiple waits or complex logic:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/multiple-named-waits.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/multiple-named-waits.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/multiple-named-waits.java"
    ```


### Combine waits with steps

Use waits between steps to implement delays in your workflow:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/wait-between-steps.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/wait-between-steps.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/wait-between-steps.java"
    ```


### Avoid very short waits

Waits must be at least 1 second. For very short delays, consider if you actually need a wait:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/minimum-wait-duration.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/minimum-wait-duration.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/minimum-wait-duration.java"
    ```


[↑ Back to top](#table-of-contents)

## FAQ

### How long can a wait operation last?

There is an upper limit of 1 year - that's the maximum length of an execution.

The wait itself doesn't consume Lambda execution time, your function suspends and resumes later. However, consider cost implications of long-running executions.

### Can I cancel a wait operation?

No, once a wait operation is checkpointed, it will complete after the specified duration. Design your workflows with this in mind.

### Do waits execute in parallel?

No, waits execute sequentially in the order they appear in your code. If you need parallel operations, use `context.parallel()` or `context.map()` instead.

### How accurate are wait durations?

Wait durations are approximate. The actual resume time depends on:
- System scheduling
- Lambda cold start time
- Current system load

### Can I use waits for polling?

You can, but we recommend using `context.wait_for_condition()` instead. It simplifies polling by handling the loop logic for you:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/polling-with-condition.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/polling-with-condition.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/polling-with-condition.java"
    ```


[↑ Back to top](#table-of-contents)

## Alternatives to wait operations

### Using wait_for_callback for external responses

When you need to wait for an external system to respond, use `context.wait_for_callback()`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/wait-for-external-response.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/wait-for-external-response.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/wait-for-external-response.java"
    ```


See [Callbacks](callbacks.md) for more details.

### Using wait_for_condition for polling

When you need to poll until a condition is met, use `context.wait_for_condition()`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/wait-for-condition.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/wait-for-condition.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/wait-for-condition.java"
    ```


[↑ Back to top](#table-of-contents)

## Testing

### Testing wait operations

You can verify wait operations in your tests by inspecting the operations list:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/test-multiple-waits.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/test-multiple-waits.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/test-multiple-waits.java"
    ```


### Testing multiple waits

When testing functions with multiple waits, you can verify each wait individually:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/test-multiple-waits.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/test-multiple-waits.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/test-multiple-waits.java"
    ```


### Testing named waits

Named waits are easier to identify in tests:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/wait/test-named-wait.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/wait/test-named-wait.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/wait/test-named-wait.java"
    ```


[↑ Back to top](#table-of-contents)

## See also

- [Steps](steps.md) - Execute business logic with automatic checkpointing
- [Callbacks](callbacks.md) - Wait for external system responses
- [Getting Started](../getting-started.md) - Learn the basics of durable functions

[↑ Back to top](#table-of-contents)

[↑ Back to main index](../index.md)

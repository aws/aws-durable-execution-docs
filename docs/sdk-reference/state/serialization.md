# Serialization

## Checkpointed data transformation

The SDK serializes durable operation results to checkpoint storage. When your durable
function replays the SDK uses those stored results rather than re-running the code
wrapped inside the operation. A SerDes (serializer/deserializer) is the object that
serializes the operation result, and deserializes it during replay.

Each SDK ships a default SerDes that handles the most common types. You only need a
custom SerDes when the default SerDes cannot handle your data types, or when you need
special behavior such as encryption, compression or writing to external storage.

The following example uses no SerDes configuration. The SDK serializes and deserializes
the step result automatically.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/walkthrough.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/walkthrough.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/serialization/Walkthrough.java"
    ```

## Lambda handler serialization

The Durable Execution SDK SerDes only applies to durable operation results. It does not
affect the final return value of your Lambda handler, which Lambda serializes
separately.

=== "TypeScript"

    Lambda serializes handler return values with `JSON.stringify`. The same types that work
    with `defaultSerdes` work as handler return values.

=== "Python"

    Lambda serializes handler return values with `json.dumps`. Types like `datetime` and
    `Decimal` are safe inside steps because `ExtendedTypeSerDes` handles them, but returning
    them directly from your handler raises
    `TypeError: Object of type X is not JSON serializable`. This is a common error when
    working with Amazon DynamoDB results. Convert those values to JSON-safe types before
    returning from the handler.

=== "Java"

    Lambda serializes handler return values with Jackson. The same `JacksonSerDes`
    configuration applies to both step results and handler return values when you use
    `DurableHandler`.

## Default serialization

Each SDK uses a default SerDes when you do not provide one.

=== "TypeScript"

    The default is `defaultSerdes`, which uses `JSON.stringify` to serialize and
    `JSON.parse` to deserialize. It handles any value that `JSON.stringify` accepts.

=== "Python"

    The default is `ExtendedTypeSerDes`. It uses plain JSON for primitives (`None`, `str`,
    `int`, `float`, `bool`, and lists of primitives) and an envelope format for everything
    else. The envelope format preserves the exact Python type through the round-trip.

    Supported types beyond primitives: `datetime`, `date`, `Decimal`, `UUID`, `bytes`,
    `bytearray`, `memoryview`, `tuple`, `list`, `dict`, and `BatchResult`.

=== "Java"

    The default is `JacksonSerDes`, which uses Jackson's `ObjectMapper`. It supports Java 8
    time types, serializes dates as ISO-8601 strings, and ignores unknown properties during
    deserialization.

    Pass a custom `ObjectMapper` to the `JacksonSerDes` constructor to override the default
    configuration.

## SerDes interface definition

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/serdes-interface.ts"
    ```

    **Parameters:**

    - `serialize` An async function that receives the value and a `SerdesContext`, and
        returns `Promise<string | undefined>`.
    - `deserialize` An async function that receives the serialized string and a
        `SerdesContext`, and returns `Promise<T | undefined>`.

    Both methods are async so that implementations can interact with external services such
    as S3 or KMS.

    **SerdesContext fields:**

    - `entityId` The operation ID for the current step or operation.
    - `durableExecutionArn` The ARN of the current durable execution.

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/serdes-interface.py"
    ```

    **Parameters:**

    - `serialize(value, serdes_context)` Converts the value to a string.
    - `deserialize(data, serdes_context)` Converts the string back to the original type.

    **SerDesContext fields:**

    - `operation_id` The operation ID for the current step or operation.
    - `durable_execution_arn` The ARN of the current durable execution.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/serialization/SerdesInterface.java"
    ```

    **Parameters:**

    - `serialize(Object value)` Converts the value to a JSON string. Returns `null` if
        `value` is `null`.
    - `deserialize(String data, TypeToken<T> typeToken)` Converts the JSON string back to
        type `T`. Returns `null` if `data` is `null`.

    Use `TypeToken<T>` to capture generic type information that Java erases at runtime. For
    example: `new TypeToken<List<String>>() {}`.

## Custom SerDes example

Implement the SerDes interface when the default cannot handle your types, or when you
need special behavior such as encryption or compression.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/custom-serdes.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/custom-serdes.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/serialization/OrderSerDes.java"
    ```

## Custom SerDes on durable operations

Pass a SerDes instance in the configuration object for the operation you want to
customize. The SDK uses that SerDes for that operation only. Other operations in the
same handler continue to use the default.

### StepConfig

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/step-config.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/step-config.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/serialization/StepConfigExample.java"
    ```

### CallbackConfig

The callback SerDes controls how the SDK deserializes the payload that an external
system sends when it completes the callback.

=== "TypeScript"

    In TypeScript, the callback SerDes only needs `deserialize`. The `serialize` method is
    not used because the external system provides the payload directly.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/callback-config.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/callback-config.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/serialization/CallbackConfigExample.java"
    ```

### MapConfig & ParallelConfig

Map and parallel perations support two SerDes fields that apply at different levels.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/map-config.ts"
    ```

    - `itemSerdes` serializes each item result.
    - `serdes` serializes the aggregated `BatchResult`.
    - `ParallelConfig` has the same two fields.

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/map-config.py"
    ```

    - `item_serdes` serializes each item result.
    - `serdes` serializes the aggregated `BatchResult`.
    - When you provide only `serdes`, the SDK uses it for both for backward compatibility.
        `ParallelConfig` has the same two fields.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/serialization/MapConfigExample.java"
    ```

    - `serDes` applies to each item result.
    - Java `ParallelConfig` does not have a `serDes` field.

## Built-in SerDes helpers

=== "TypeScript"

    `createClassSerdes(cls)` creates a `Serdes<T>` that preserves class instances through
    the round-trip. It deserializes by calling `Object.assign(new cls(), JSON.parse(data))`,
    so class methods are available on the deserialized value. The constructor must take no
    required parameters. Private fields and getters are not preserved.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/builtin-helpers.ts"
    ```

=== "Python"

    `PassThroughSerDes` stores the value as-is (the value must already be a string).
    `JsonSerDes` uses `json.dumps` and `json.loads` without the envelope format that
    `ExtendedTypeSerDes` adds for complex types.

    ```python
    --8<-- "examples/python/sdk-reference/serialization/builtin-helpers.py"
    ```

=== "Java"

    The built-in `JacksonSerDes` handles class instances via Jackson's `ObjectMapper`. You
    can create your own passthrough SerDes like this:

    ```java
    --8<-- "examples/java/sdk-reference/serialization/PassThroughSerdesExample.java"
    ```

## Serialization errors

When serialization or deserialization fails, each SDK raises or throws a specific
exception type. See
[Serialization errors](../error-handling/errors.md#serialization-errors) for the
exception hierarchy and how to handle serialization failures.

## See also

- [StepConfig](../operations/step.md#stepconfig) Step serialization configuration
- [CallbackConfig](../operations/callback.md#callbackconfig) Callback serialization
    configuration
- [MapConfig](../operations/map.md#mapconfig) Map serialization configuration
- [Serialization errors](../error-handling/errors.md#serialization-errors) Serialization
    error types

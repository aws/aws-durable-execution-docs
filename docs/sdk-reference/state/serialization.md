# Serialization

Learn how the SDK serializes and deserializes data for durable execution checkpoints.

## Table of Contents

- [Terminology](#terminology)
- [What is serialization?](#what-is-serialization)
- [Key features](#key-features)
- [Default serialization behavior](#default-serialization-behavior)
- [Supported types](#supported-types)
- [Converting non-serializable types](#converting-non-serializable-types)
- [Custom serialization](#custom-serialization)
- [Serialization in configurations](#serialization-in-configurations)
- [Best practices](#best-practices)
- [Troubleshooting](#troubleshooting)
- [FAQ](#faq)

[← Back to main index](../index.md)

## Terminology

**Serialization** - Converting Python objects to strings for storage in checkpoints.

**Deserialization** - Converting checkpoint strings back to Python objects.

**SerDes** - Short for Serializer/Deserializer, a custom class that handles both serialization and deserialization.

**Checkpoint** - A saved state of execution that includes serialized operation results.

**Extended types** - Types beyond basic JSON (datetime, Decimal, UUID, bytes) that the SDK serializes automatically.

**Envelope format** - The SDK's internal format that wraps complex types with type tags for accurate deserialization.

[↑ Back to top](#table-of-contents)

## What is serialization?

Serialization converts Python objects into strings that can be stored in checkpoints. When your durable function resumes, deserialization converts those strings back into Python objects. The SDK handles this automatically for most types.

[↑ Back to top](#table-of-contents)

## Key features

- Automatic serialization for common Python types
- Extended type support (datetime, Decimal, UUID, bytes)
- Custom serialization for complex objects
- Type preservation during round-trip serialization
- Efficient plain JSON for primitives

[↑ Back to top](#table-of-contents)

## Default serialization behavior

The SDK handles most Python types automatically:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/default-behavior.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/default-behavior.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/default-behavior.java"
    ```


The SDK serializes data automatically when:
- Checkpointing step results
- Storing callback payloads
- Passing data to child contexts
- Returning results from your handler

[↑ Back to top](#table-of-contents)

## Supported types

### Primitive types

These types serialize as plain JSON for performance:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/primitive-types.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/primitive-types.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/primitive-types.java"
    ```


**Supported primitive types:**
- `None`
- `str`
- `int`
- `float`
- `bool`
- Lists containing only primitives

[↑ Back to top](#table-of-contents)

### Extended types

The SDK automatically handles these types using envelope format:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/extended-types.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/extended-types.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/extended-types.java"
    ```


**Supported extended types:**
- `datetime` - ISO format with timezone
- `date` - ISO date format
- `Decimal` - Precise decimal numbers
- `UUID` - Universally unique identifiers
- `bytes`, `bytearray`, `memoryview` - Binary data (base64 encoded)
- `tuple` - Immutable sequences
- `list` - Mutable sequences (including nested)
- `dict` - Dictionaries (including nested)

[↑ Back to top](#table-of-contents)

### Container types

Containers can hold any supported type, including nested containers:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/container-types.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/container-types.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/container-types.java"
    ```


[↑ Back to top](#table-of-contents)

## Converting non-serializable types

Some Python types aren't serializable by default. Convert them before passing to durable operations.

### Dataclasses

Convert dataclasses to dictionaries:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/dataclasses.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/dataclasses.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/dataclasses.java"
    ```


### Pydantic models

Use Pydantic's built-in serialization:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/convert-to-dicts.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/convert-to-dicts.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/convert-to-dicts.java"
    ```


### Custom objects

Implement `to_dict()` and `from_dict()` methods:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/custom-objects.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/custom-objects.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/custom-objects.java"
    ```


[↑ Back to top](#table-of-contents)

## Custom serialization

Implement custom serialization for specialized needs like encryption or compression.

### Creating a custom SerDes

Extend the `SerDes` base class:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/custom-serdes.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/custom-serdes.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/custom-serdes.java"
    ```


### Using custom SerDes with steps

Pass your custom SerDes in `StepConfig`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/use-custom-serdes.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/use-custom-serdes.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/use-custom-serdes.java"
    ```


### Encryption example

Encrypt sensitive data in checkpoints:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/encryption.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/encryption.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/encryption.java"
    ```


[↑ Back to top](#table-of-contents)

## Serialization in configurations

Different operations support custom serialization through their configuration objects.

### StepConfig

Control serialization for step results:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/step-config.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/step-config.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/step-config.java"
    ```


### CallbackConfig

Control serialization for callback payloads:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/callback-config.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/callback-config.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/callback-config.java"
    ```


### MapConfig and ParallelConfig

Control serialization for batch results:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/map-parallel-config.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/map-parallel-config.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/map-parallel-config.java"
    ```


**Note:** When both `serdes` and `item_serdes` are provided:
- `item_serdes` serializes individual item results in child contexts
- `serdes` serializes the entire `BatchResult` at the handler level

For backward compatibility, if only `serdes` is provided, it's used for both individual items and the `BatchResult`.

[↑ Back to top](#table-of-contents)

## Best practices

### Use default serialization when possible

The SDK handles most cases efficiently without custom serialization:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/use-default-serialization.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/use-default-serialization.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/use-default-serialization.java"
    ```


### Convert complex objects to dicts

Convert custom objects to dictionaries before passing to durable operations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/convert-to-dicts.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/convert-to-dicts.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/convert-to-dicts.java"
    ```


### Keep serialized data small

Large checkpoints might slow down execution. Keep data compact:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/keep-data-small.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/keep-data-small.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/keep-data-small.java"
    ```


### Use appropriate types

Choose types that serialize efficiently:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/appropriate-types.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/appropriate-types.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/appropriate-types.java"
    ```


### Test serialization round-trips

Verify your data survives serialization:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/test-round-trips.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/test-round-trips.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/test-round-trips.java"
    ```


### Handle serialization errors gracefully

Catch and handle serialization errors:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/handle-errors-gracefully.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/handle-errors-gracefully.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/handle-errors-gracefully.java"
    ```


[↑ Back to top](#table-of-contents)

## Troubleshooting

### Unsupported type error

**Problem:** `SerDesError: Unsupported type: <class 'MyClass'>`

**Solution:** Convert custom objects to supported types:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/unsupported-type-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/unsupported-type-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/unsupported-type-error.java"
    ```


### Serialization failed error

**Problem:** `ExecutionError: Serialization failed for id: step-123`

**Cause:** The data contains types that can't be serialized.

**Solution:** Check for circular references or unsupported types:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/serialization-failed-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/serialization-failed-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/serialization-failed-error.java"
    ```


### Type not preserved after deserialization

**Problem:** `tuple` becomes `list` or `Decimal` becomes `float`

**Cause:** Using a custom SerDes that doesn't preserve types.

**Solution:** Use default serialization which preserves types:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/type-not-preserved.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/type-not-preserved.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/type-not-preserved.java"
    ```


### Large payload errors

**Problem:** Checkpoint size exceeds limits

**Solution:** Reduce data size or use summary generators:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/large-payload-errors.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/large-payload-errors.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/large-payload-errors.java"
    ```


### Datetime timezone issues

**Problem:** Datetime loses timezone information

**Solution:** Always use timezone-aware datetime objects:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/datetime-timezone.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/datetime-timezone.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/datetime-timezone.java"
    ```


[↑ Back to top](#table-of-contents)

## FAQ

### What types can I serialize?

The SDK supports:
- Primitives: `None`, `str`, `int`, `float`, `bool`
- Extended: `datetime`, `date`, `Decimal`, `UUID`, `bytes`, `tuple`
- Containers: `list`, `dict` (including nested)

For other types, convert to dictionaries first.

### Do I need custom serialization?

Most applications don't need custom serialization. Use it for:
- Encryption of sensitive data
- Compression of large payloads
- Special encoding requirements
- Legacy format compatibility

### How does serialization affect performance?

The SDK optimizes for performance:
- Primitives use plain JSON (fast)
- Extended types use envelope format (slightly slower but preserves types)
- Custom SerDes adds overhead based on your implementation

### Can I serialize Pydantic models?

Yes, convert them to dictionaries:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/faq-pydantic-models.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/faq-pydantic-models.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/faq-pydantic-models.java"
    ```


### What's the difference between serdes and item_serdes?

In `MapConfig` and `ParallelConfig`:
- `item_serdes`: Serializes individual item results in child contexts
- `serdes`: Serializes the entire `BatchResult` at handler level

If only `serdes` is provided, it's used for both (backward compatibility).

### How do I handle binary data?

Use `bytes` type - it's automatically base64 encoded:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/faq-binary-data.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/faq-binary-data.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/faq-binary-data.java"
    ```


### Can I use JSON strings directly?

Yes, use `PassThroughSerDes` or `JsonSerDes`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/faq-json-strings.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/faq-json-strings.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/faq-json-strings.java"
    ```


### What happens if serialization fails?

The SDK raises `ExecutionError` with details. Handle it in your code:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/faq-serialization-fails.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/faq-serialization-fails.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/faq-serialization-fails.java"
    ```


### How do I debug serialization issues?

Test serialization independently:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/serialization/faq-debug-issues.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/serialization/faq-debug-issues.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/serialization/faq-debug-issues.java"
    ```


### Are there size limits for serialized data?

Yes, checkpoints have size limits (typically 256KB). Keep data compact:
- Only checkpoint necessary data
- Use summary generators for large results
- Store large data externally (S3) and checkpoint references

[↑ Back to top](#table-of-contents)

## See also

- [Steps](../core/steps.md) - Using steps with custom serialization
- [Callbacks](../core/callbacks.md) - Serializing callback payloads
- [Map Operations](../core/map.md) - Serialization in map operations
- [Error Handling](error-handling.md) - Handling serialization errors
- [Best Practices](../best-practices.md) - General best practices

[↑ Back to index](#table-of-contents)

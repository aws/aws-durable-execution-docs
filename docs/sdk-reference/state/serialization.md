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

## FileSystem serdes

The FileSystem serdes stores data on the filesystem and keeps a file pointer in
the checkpoint. This can be useful when you do not want to store your data in the
checkpoint itself, for example when your payload exceeds the durable execution
checkpoint size limit. The persisted value lives on the filesystem and the SDK
reads it on replay.

The FileSystem serdes requires a filesystem that persists across invocations and
that every Lambda execution environment can read. In AWS Lambda, this means:

- [Amazon Elastic File System (Amazon EFS)](https://docs.aws.amazon.com/lambda/latest/dg/configuration-filesystem-efs.html).
    Serverless file system that scales automatically with your workloads.
- [Amazon S3 Files](https://docs.aws.amazon.com/lambda/latest/dg/configuration-filesystem-s3files.html).
    Serverless file system for mounting an Amazon S3 bucket. Amazon S3 Files
    provides access to Amazon S3 objects as files using standard file system
    operations such as read and write on the local mount path.

!!! warning "Do not use Lambda's `/tmp` directory"

    Lambda's `/tmp` filesystem is local to a single execution environment. A
    different execution environment may run the next invocation, so the SDK cannot
    find files written to `/tmp` on replay and deserialization fails. Always mount
    a persistent, shared filesystem.

=== "TypeScript"

    Pass the FileSystem serdes to a single operation through `StepConfig`,
    `CallbackConfig`, `MapConfig`, or `ParallelConfig`. Other operations in the
    same handler continue to use the default serdes.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/filesystem-serdes-walkthrough.ts"
    ```

=== "Python"

    Pass the FileSystem serdes to a single operation through `StepConfig`,
    `CallbackConfig`, `MapConfig`, or `ParallelConfig`. Other operations in the
    same handler continue to use the default serdes.

    ```python
    --8<-- "examples/python/sdk-reference/serialization/filesystem-serdes-walkthrough.py"
    ```

=== "Java"

    Coming soon. See
    [aws-durable-execution-sdk-java#463](https://github.com/aws/aws-durable-execution-sdk-java/issues/463).

### Create a FileSystem serdes

Create a FileSystem serdes and pass it to an operation's config.

=== "TypeScript"

    `createFileSystemSerdes(basePath, config?)` returns a `Serdes<unknown>`.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/filesystem-serdes-signature.ts"
    ```

    **Parameters:**

    - `basePath` Directory where the SDK writes data files. Set this to your
        filesystem mount point.
    - `config` (optional) `FileSystemSerdesConfig` controlling storage mode, path
        encoding, and preview generation.

    **Returns:** A `Serdes` that reads and writes JSON files under `basePath`.

=== "Python"

    `FileSystemSerDes(base_path, config?)` is a `SerDes` subclass you instantiate.

    ```python
    --8<-- "examples/python/sdk-reference/serialization/filesystem-serdes-signature.py"
    ```

    **Parameters:**

    - `base_path` Directory where the SDK writes data files. Set this to your
        filesystem mount point.
    - `config` (optional) `FileSystemSerDesConfig` controlling storage mode, path
        encoding, and preview generation.

    **Returns:** A `SerDes` instance that reads and writes JSON files under `base_path`.

=== "Java"

    Coming soon.

### FileSystemSerdesConfig

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/filesystem-serdes-config.ts"
    ```

    **Fields:**

    - `storageMode` (optional) A `FileSystemSerdesMode` value. Default:
        `FileSystemSerdesMode.ALWAYS`.
    - `pathEncoding` (optional) A `FileSystemPathEncoding` value. Default:
        `FileSystemPathEncoding.URI`.
    - `generatePreview` (optional) Function that returns a preview object. The SDK
        stores the preview inline in the checkpoint envelope alongside the file
        pointer. Use the [`buildPreview`](#preview-and-pii-masking) helper or
        write your own.

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/filesystem-serdes-config.py"
    ```

    **Fields:**

    - `storage_mode` (optional) A `FileSystemSerDesMode` value. Default:
        `FileSystemSerDesMode.ALWAYS`.
    - `path_encoding` (optional) A `FileSystemPathEncoding` value. Default:
        `FileSystemPathEncoding.URI`.
    - `generate_preview` (optional) Callable that returns a preview dict or `None`.
        The SDK stores the preview inline in the checkpoint envelope alongside the
        file pointer. Use the [`build_preview`](#preview-and-pii-masking) helper or
        write your own.
    - `serdes` (optional) `SerDes` instance that serializes values before writing
        them to the filesystem. Default: `ExtendedTypeSerDes`.

=== "Java"

    Coming soon.

### Storage modes

The `storageMode` enumerated field controls when the SDK writes to the filesystem.

`FileSystemSerdesMode.ALWAYS` writes every value to a file. The checkpoint stores
only the file pointer.

`FileSystemSerdesMode.OVERFLOW` uses the standard durable execution checkpoint
store and only writes to the filesystem when the value would exceed the durable
execution checkpoint size limit. See
[AWS Lambda service quotas](https://docs.aws.amazon.com/general/latest/gr/lambda-service.html).

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/filesystem-serdes-overflow.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/filesystem-serdes-overflow.py"
    ```

=== "Java"

    Coming soon.

### Path encoding

The `pathEncoding` field controls how the durable execution ARN and the entity ID
become the on-disk directory and file names.

`FileSystemPathEncoding.URI` builds a per-execution directory from the function
name, execution name, and invocation ID parsed from the ARN, and encodes the entity
ID with `encodeURIComponent` for the file name. Names stay readable when you read
files directly from the mount. A very long entity ID may exceed the filesystem's
per-name length limit, commonly 255 bytes.

`FileSystemPathEncoding.HASH` replaces the ARN and the entity ID with their SHA-256
hex digests. Names are a fixed 64 characters and are always filesystem-safe
regardless of the input characters or length. Choose `HASH` when entity IDs may
contain characters that are unsafe in a file name, such as `/`, or may be long
enough to exceed the name-length limit.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/filesystem-serdes-path-encoding.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/filesystem-serdes-path-encoding.py"
    ```

=== "Java"

    Coming soon.

### Preview and PII masking

When the FileSystem serdes writes to a file, the checkpoint envelope only contains
the file pointer, so the GetDurableExecution API and the AWS Console cannot show
the actual data for that operation. Configure `generatePreview` to embed a small
object inline so you can see the data without reading the file.

Use `buildPreview` to compute the preview from a `PreviewConfig`. The config
selects which fields to include, exclude, or mask. Masking replaces a field's value
with `***` or a custom string. The preview object is capped at 4096 bytes by
default.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/filesystem-serdes-preview.ts"
    ```

    **`PreviewConfig` fields:**

    - `mode` Either `PreviewMode.INCLUDE_ALL` or `PreviewMode.EXCLUDE_ALL`. Sets
        the starting point before applying `exclude` and `mask`.
    - `include` (optional) Fields to add when starting from `EXCLUDE_ALL`.
    - `exclude` (optional) Fields to remove. Always wins over `mask`.
    - `mask` (optional) Fields whose values become `maskString`. A masked field is
        visible in the preview unless it is also excluded.
    - `maskString` (optional) Replacement value for masked fields. Default: `***`.
    - `maxPreviewBytes` (optional) Maximum size in bytes for the preview object
        when JSON-serialized. Default: `4096`.

    Each `PreviewField` selector has a `name` and an optional `match`. Use
    `FieldMatchMode.ANYWHERE` (default) to match the field name at any depth in
    the object tree, or `FieldMatchMode.PATH` to match an exact dot-notation path
    from the root.

    Selectors cannot address field names that contain a dot because a dot is
    indistinguishable from a path separator. The preview merges array elements
    into a plain object at the array's path rather than preserving array structure.

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/filesystem-serdes-preview.py"
    ```

    **`PreviewConfig` fields:**

    - `mode` Either `PreviewMode.INCLUDE_ALL` or `PreviewMode.EXCLUDE_ALL`. Sets
        the starting point before applying `exclude` and `mask`.
    - `include` (optional) Fields to add when starting from `EXCLUDE_ALL`.
    - `exclude` (optional) Fields to remove. Always wins over `mask`.
    - `mask` (optional) Fields whose values become `mask_string`. A masked field is
        visible in the preview unless it is also excluded.
    - `mask_string` (optional) Replacement value for masked fields. Default: `"***"`.
    - `max_preview_bytes` (optional) Maximum size in bytes for the preview object
        when JSON-serialized. Default: `4096`.

    Each `PreviewField` selector has a `name` and an optional `match`. Use
    `FieldMatchMode.ANYWHERE` (default) to match the field name at any depth in
    the object tree, or `FieldMatchMode.PATH` to match an exact dot-notation path
    from the root.

    Selectors cannot address field names that contain a dot because a dot is
    indistinguishable from a path separator. The preview merges array elements
    into a plain object at the array's path rather than preserving array structure.

=== "Java"

    Coming soon.

### Set as the default for the handler

When you want every step result, child-context result, invoke result, and
waitForCondition result in the handler to use the FileSystem serdes, configure it
once with `configureSerdes`.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/filesystem-serdes-default.ts"
    ```

=== "Python"

    Create a single `FileSystemSerDes` instance and a shared `StepConfig`, then
    pass it to every operation in the handler.

    ```python
    --8<-- "examples/python/sdk-reference/serialization/filesystem-serdes-default.py"
    ```

=== "Java"

    Coming soon. See
    [aws-durable-execution-sdk-java#463](https://github.com/aws/aws-durable-execution-sdk-java/issues/463).

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

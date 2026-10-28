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

=== "Go"

    ```go
    --8<-- "examples/go/sdk-reference/serialization/walkthrough.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/sdk-reference/serialization/Walkthrough.cs"
    ```

## Round-trip serialization

The SDK round-trips an operation result by serializing and then deserializing it
before returning it on the first run. This round trip ensures that the first run
returns the same value that replay reconstructs from the operation's checkpoint.

A serializer/deserializer pair should preserve the value across a round trip.
`deserialize(serialize(value))` should be equivalent to `value`. If it is not,
the round trip will change the operation result.

## Lambda handler serialization

Handler input and output serialization varies by SDK. An operation-specific
serializer changes that operation's payloads without changing handler input and
output. Follow the language-specific configuration below.

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

=== "Go"

    The SDK decodes the handler's input and encodes its return value with
    `encoding/json`, with HTML escaping disabled. A `Serdes` set with `WithSerdes` or
    `ConfigureSerdes` does not affect them. It applies only to durable operation results.

=== "C#"

    The registered `ILambdaSerializer` serializes everything: durable operation results
    and the handler's final return value. There is no separate serializer for operation
    results, so the same configuration always applies to both.

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

=== "Go"

    The default is `JSONSerdes`, which encodes with `encoding/json` and HTML escaping
    disabled, so `<`, `>`, and `&` stay literal in the stored JSON. Map keys are sorted
    and a nil value encodes as null. It handles any value `encoding/json` accepts. A
    type that does not round-trip through `encoding/json` needs a custom `Serdes`.

=== "C#"

    There is no per-operation default SerDes. Register a single `ILambdaSerializer` at the
    host boundary and the SDK uses it for every durable operation result.

    Use `DefaultLambdaJsonSerializer` (from `Amazon.Lambda.Serialization.SystemTextJson`)
    for reflection-based serialization. For AOT or trim-friendly functions, use
    `SourceGeneratorLambdaJsonSerializer<TContext>` and register your `JsonSerializerContext`.

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

    Operation payloads use the overloads that accept `SerDesContext`, added in
    SDK 2.3.0. `durableExecutionArn()` identifies the execution, and `entityId()`
    identifies the operation payload and its role, such as result or exception.
    Default overload implementations delegate to the original methods, keeping
    existing serializers compatible. A storage serializer must preserve old
    references when the same entity is serialized again.

    Use `TypeToken<T>` to capture generic type information that Java erases at runtime. For
    example: `new TypeToken<List<String>>() {}`.

=== "Go"

    The interface is `Serdes`. Its methods are `Marshal` and `Unmarshal`, which work
    with byte slices and take a `context.Context` for external calls.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/serdes-interface.go"
    ```

    **Methods:**

    - `Marshal(ctx, meta, v)` Converts the value to bytes.
    - `Unmarshal(ctx, meta, data, v)` Fills the pointer `v` from the bytes.

    **SerdesContext fields:**

    - `OperationID` The operation ID for the current step or operation.
    - `DurableExecutionArn` The ARN of the current durable execution.

    The interface takes `any`, so one `Serdes` serves every result type. For a serdes
    written for one type, use `SerdesOf`. A serdes failure is permanent by default.
    Return `RetryableSerdesError` to make it transient.

=== "C#"

    .NET has no per-operation SerDes interface. Serialization is controlled by the single
    `ILambdaSerializer` registered on `ILambdaContext.Serializer`.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/serialization/SerdesInterface.cs"
    ```

    **Methods:**

    - `Serialize<T>(T response, Stream responseStream)` Writes the serialized form of
        `response` to `responseStream`.
    - `Deserialize<T>(Stream requestStream)` Reads `requestStream` and returns the
        deserialized value of type `T`.

    The same `ILambdaSerializer` handles every durable operation result and the handler's
    return value.

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

=== "Go"

    Implement `Marshal` and `Unmarshal` on a type. For a serdes that handles one result
    type, `SerdesOf` is shorter, as the next example shows.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/custom-serdes.go"
    ```

=== "C#"

    Implement `ILambdaSerializer` and register it at the host boundary. The custom
    serializer applies to every durable operation result, not a single operation.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/serialization/OrderSerDes.cs"
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

=== "Go"

    Pass `WithStepSerdes` to override the serializer for one step's result. Other
    operations keep the default. The other per-operation options are `WithChildSerdes`,
    `WithInvokePayloadSerdes`, `WithInvokeResultSerdes`, and `WithTypedBranchSerdes`. A
    per-operation option takes precedence over the handler default set with `WithSerdes`
    or `ConfigureSerdes`.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/step-config.go"
    ```

=== "C#"

    `StepConfig` does not expose a serializer. The step result is serialized with the
    `ILambdaSerializer` registered at the host boundary. Register a custom
    `ILambdaSerializer` there to change how step results are serialized.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/serialization/StepConfigExample.cs"
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

=== "Go"

    The default callback deserializer is `RawSerdes`, which returns the submitted bytes
    unchanged. So by default the result type must be `string`, `[]byte`, or
    `json.RawMessage`. Any other type fails with a `*SerdesError`. Pass
    `WithCallbackSerdes(JSONSerdes)` to decode a JSON payload into a struct.

    For a callback payload, the per-operation `WithCallbackSerdes` takes precedence over
    the handler-wide `Deserializer`. When neither is set, the payload passes through
    `RawSerdes`. Set the handler-wide `Deserializer` with `WithCallbackDeserializer`, or
    from inside the handler with the `CallbackDeserializer` field of `ConfigureSerdes`.
    A `Deserializer` has one method, `Unmarshal(data []byte, v any) error`. The
    handler-level `WithSerdes` does not apply to callback payloads. `WaitForCallback`
    accepts the same `WithCallbackSerdes` option and decodes the payload once, at the
    `WaitForCallback` operation.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/callback-config.go"
    ```

=== "C#"

    `CallbackConfig` does not expose a serializer. The payload the external system delivers
    is deserialized with the `ILambdaSerializer` registered at the host boundary. Register a
    custom `ILambdaSerializer` there to change how the callback payload is deserialized.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/serialization/CallbackConfigExample.cs"
    ```

### MapConfig & ParallelConfig

Map and parallel operations can customize serialization for individual results.
Some SDKs also support a serializer for the aggregate result.

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

=== "Go"

    Two batch options apply to both `Map` and `Parallel`:

    - `WithBatchSerdes` serializes each item result. Default: the handler serdes.
    - `WithBatchResultSerdes` serializes the aggregated `BatchResult`. Without it, the SDK
        stores the `BatchResult` through the item serdes.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/map-config.go"
    ```

=== "C#"

    `MapConfig` does not expose a serializer, and there is no separate item-level
    serializer. Each item result is serialized with the `ILambdaSerializer` registered at
    the host boundary. `ParallelConfig` likewise has no serializer field.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/serialization/MapConfigExample.cs"
    ```

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

=== "Go"

    `JSONSerdes` is the default. `RawSerdes` stores the value as-is. The value must be a
    `string`, `[]byte`, or `json.RawMessage`. `SerdesOf` is a generic adapter that turns
    typed marshal and unmarshal functions into a `Serdes`. The SDK needs no class
    serdes, because a struct's methods are available on any value after a JSON round
    trip.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/builtin-helpers.go"
    ```

=== "C#"

    `DefaultLambdaJsonSerializer` handles class and record instances via System.Text.Json.
    You can create a pass-through serializer that stores string values as-is by implementing
    `ILambdaSerializer` and registering it at the host boundary:

    ```csharp
    --8<-- "examples/csharp/sdk-reference/serialization/PassThroughSerdesExample.cs"
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
    operations such as read and write on the local mount path. A mount with
    delayed remote synchronization can lose recent writes after a runtime
    failure even if a local flush succeeded. Account for that durability
    boundary when choosing storage for checkpointed results.

Retain result files for the execution's full replay lifetime and arrange cleanup
externally. The filesystem serializers do not delete published result files.

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

    `FileSystemSerDes` ships in the main Java SDK artifact starting with 2.3.0.
    Configure it per operation with `StepConfig.serDes`,
    `RunInChildContextConfig.serDes`, `MapConfig.serDes`,
    `ParallelBranchConfig.serDes`, or `WaitForConditionConfig.serDes`.

    ```java
    --8<-- "examples/java/sdk-reference/serialization/filesystem-serdes-walkthrough.java"
    ```

    The step stores the large report in a file and checkpoints its reference.
    After the wait, replay reads that file without running the step again. The
    handler returns only the report length, keeping its output small.

    Every successful serialization creates a new immutable file, including
    repeated writes for one entity during retries or polling.

    Callback and invoke results must already use the filesystem envelope and
    a shared mount accessible at the same absolute path. Ordinary JSON does
    not become a file reference automatically. For a normally encoded invoke
    request and an offloaded response, use
    `InvokeConfig.builder().payloadSerDes(new JacksonSerDes()).serDes(files)`.
    See the [Java filesystem guide](https://github.com/aws/aws-durable-execution-sdk-java/blob/v3.0.0/docs/advanced/filesystem-serdes.md)
    for producer-side serialization.

=== "Go"

    Create a FileSystem serdes with `NewFileSystemSerdes` and pass it to one operation
    with an option such as `WithStepSerdes`. The file holds the value as JSON. The
    checkpoint holds an envelope with the full file path.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/filesystem-serdes-walkthrough.go"
    ```

=== "C#"

    Not available. The .NET SDK has no FileSystem serdes. To keep large payloads out of the
    checkpoint, write them to a persistent store (such as Amazon S3) inside a step and
    checkpoint a pointer instead of the payload.

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

    `FileSystemSerDes.builder(Path)` returns a builder. Call `build()` to create
    the immutable serializer; construction does not write files.

    ```java
    --8<-- "examples/java/sdk-reference/serialization/filesystem-serdes-signature.java"
    ```

    Set `basePath` to a persistent shared mount. The builder normalizes it to
    an absolute path. Reads reject references outside that base and symlinks
    below it. Keep the mount and base directory under application control.
    Missing files and I/O failures throw `SerDesException`.

    The SDK supplies `SerDesContext` for operation payloads. Calling the
    context-free `serialize` or `deserialize` overload throws
    `SerDesException`.

=== "Go"

    `NewFileSystemSerdes(basePath, cfg...)` returns a `Serdes`. The config is a trailing
    variadic argument. Omit it for the defaults. The SDK uses only the first config.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/filesystem-serdes-signature.go"
    ```

    **Parameters:**

    - `basePath` Directory where the SDK writes data files. Set this to your filesystem
        mount point.
    - `cfg` (optional) A `FileSystemSerdesConfig` controlling storage mode, path
        encoding, and preview generation.

    **Returns:** A `Serdes` that reads and writes JSON files under `basePath`.

=== "C#"

    Not available.

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

    Java uses a builder instead of a separate config class:

    ```java
    --8<-- "examples/java/sdk-reference/serialization/filesystem-serdes-config.java"
    ```

    - `storageMode` defaults to `FileSystemStorageMode.ALWAYS`.
    - `pathEncoding` defaults to `FileSystemPathEncoding.URI`.
    - `delegate` defaults to `JacksonSerDes`. Its serialized string is stored
        unchanged as UTF-8. A null serialized string remains null without a file.
    - `previewGenerator` optionally returns a `Map<String, Object>` alongside
        the file pointer. Return null to omit a preview.

    Custom delegates and preview generators must support concurrent calls.

=== "Go"

    ```go
    --8<-- "examples/go/sdk-reference/serialization/filesystem-serdes-config.go"
    ```

    **Fields:**

    - `Mode` A `FileSystemSerdesMode` value. Default: `FileSystemSerdesModeAlways`.
    - `PathEncoding` A `FileSystemPathEncoding` value. Default:
        `FileSystemPathEncodingURI`.
    - `GeneratePreview` A function that returns a preview map. The SDK stores the result
        inline in the checkpoint envelope next to the file reference. Use the
        [`BuildPreview`](#preview-and-pii-masking) helper or write your own.

    The FileSystem serdes always encodes values with `encoding/json` and HTML escaping
    disabled, as `JSONSerdes` does.

=== "C#"

    Not available.

### Storage modes

The storage mode controls when the SDK writes to the filesystem. Always mode
writes values to files and checkpoints their pointers. Overflow mode keeps
small values inline and offloads values that exceed its envelope limit. See
[AWS Lambda service quotas](https://docs.aws.amazon.com/general/latest/gr/lambda-service.html).

=== "TypeScript"

    `FileSystemSerdesMode.ALWAYS` writes every value except `undefined` to a file,
    including `null`. `OVERFLOW` keeps the `{"data": ...}` envelope inline when
    its UTF-8 size is at most 255 KiB, counting JSON escaping. Larger envelopes
    use a file. `undefined` remains `undefined` without a file in either mode.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/filesystem-serdes-overflow.ts"
    ```

=== "Python"

    `FileSystemSerDesMode.ALWAYS` writes every value to a file, including `None`.
    `OVERFLOW` keeps the `{"data": ...}` envelope inline when its UTF-8 size is
    at most 255 KiB, counting JSON escaping. Larger envelopes use a file.

    ```python
    --8<-- "examples/python/sdk-reference/serialization/filesystem-serdes-overflow.py"
    ```

=== "Java"

    `FileSystemStorageMode.OVERFLOW` stores an inline envelope when it fits
    within 255 KiB, counting JSON escaping and UTF-8 bytes. Larger envelopes
    use a file. `ALWAYS`, the default, offloads every non-null serialized value.

    ```java
    --8<-- "examples/java/sdk-reference/serialization/filesystem-serdes-overflow.java"
    ```

=== "Go"

    In overflow mode, the FileSystem serdes writes the value to a file when the
    inline envelope exceeds 255KB. `GeneratePreview` is not called for a value
    stored inline.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/filesystem-serdes-overflow.go"
    ```

=== "C#"

    Not available.

### Path encoding

Path encoding controls how the durable execution ARN and operation identity
become the on-disk directory and file names.

URI encoding keeps identity components readable through percent encoding.
Hash encoding derives compact path components from SHA-256 digests. Exact paths
vary by SDK. Use hash encoding when an identity could exceed the filesystem's
per-name length limit, commonly 255 bytes.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/serialization/filesystem-serdes-path-encoding.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/serialization/filesystem-serdes-path-encoding.py"
    ```

=== "Java"

    `URI` percent-encodes each slash-separated segment of the full execution
    ARN and the entire entity ID. `HASH` replaces the full ARN and entity ID
    with their SHA-256 hex digests. Each file also has a unique suffix so a
    later serialization cannot overwrite a checkpointed value.

    ```java
    --8<-- "examples/java/sdk-reference/serialization/filesystem-serdes-path-encoding.java"
    ```

=== "Go"

    `FileSystemPathEncodingURI`, the default, writes
    `<functionName>/<executionName>/<invocationId>/<operationID>.json` under the base
    path. It takes the first three segments from the execution ARN. It percent-encodes
    each segment, so bytes other than letters, digits, `-`, `_`, `.`, and `~` become
    `%XX`.

    `FileSystemPathEncodingHash` writes `<arnHash>/<operationID>.json`. `arnHash` is the
    hex encoding of the first 16 bytes of the SHA-256 digest of the execution ARN, so it
    is 32 characters long. `FileSystemPathEncodingHash` does not hash or encode the
    operation ID. The SDK generates operation IDs such as `1` or `1-2-3`, so the file
    name is always safe.

    The checkpoint envelope stores the full file path. So the SDK reads a value back
    correctly after you change the encoding.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/filesystem-serdes-path-encoding.go"
    ```

=== "C#"

    Not available.

### Preview and PII masking

When the FileSystem serdes writes to a file, the checkpoint envelope only contains
the file pointer, so the GetDurableExecution API and the AWS Console cannot show
the actual data for that operation. Configure a preview generator to embed a
small object inline so you can see selected data without reading the file.

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

    Set `previewGenerator` to produce an allowlisted summary. Java has no
    `buildPreview` helper or automatic 4096-byte preview cap.

    ```java
    --8<-- "examples/java/sdk-reference/serialization/filesystem-serdes-preview.java"
    ```

    The file pointer and preview together must fit within 255 KiB. An oversized
    preview fails serialization and removes the unpublished file. Previews
    appear in checkpoint APIs, so omit sensitive fields deliberately.

=== "Go"

    ```go
    --8<-- "examples/go/sdk-reference/serialization/filesystem-serdes-preview.go"
    ```

    **`PreviewConfig` fields:**

    - `Mode` Either `PreviewIncludeAll` or `PreviewExcludeAll`. Sets the starting point
        before `Exclude` and `Mask`. Default: `PreviewIncludeAll`.
    - `Include` Fields to add when starting from `PreviewExcludeAll`.
    - `Exclude` Fields to remove. Always wins over `Include` and `Mask`.
    - `Mask` Fields whose values become `MaskString`. A masked field is visible under
        either mode unless it is also excluded.
    - `MaskString` Replacement for masked fields. Default: `***`.
    - `MaxPreviewBytes` Maximum JSON size of the preview in bytes. Default: `4096`. When
        the cap leaves fields out, the preview carries the key `$truncated` set to `true`.
    - `MaxDepth` How many nested objects and slices are traversed. Default: `32`.

    Each `PreviewField` has a `Name` and a `Match`. Use `FieldMatchAnywhere`, the
    default, to match the field name at any depth, or `FieldMatchPath` for an exact
    dot-path from the root. A field name that contains a dot cannot be selected. The
    preview merges the fields of slice elements under the slice's path.

=== "C#"

    Not available.

### Set as the default for the handler

SDKs differ in whether the FileSystem serdes can be the handler-wide default.
Use the configuration for your language below.

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

    Do not pass `FileSystemSerDes` to `DurableConfig.withSerDes`: the global
    serializer also handles Lambda input and output without operation identity.
    Instead, share one serializer and operation config across the operations
    that need it:

    ```java
    --8<-- "examples/java/sdk-reference/serialization/filesystem-serdes-default.java"
    ```

=== "Go"

    Set the FileSystem serdes handler-wide from inside the handler with
    `ConfigureSerdes`, or at construction with `durable.Start(handler, WithSerdes(...))`.
    `ConfigureSerdes` must run identically on every invocation, including replays. The
    handler serdes also serves `Map` and `Parallel` item results. It does not apply to
    callback payloads.

    ```go
    --8<-- "examples/go/sdk-reference/serialization/filesystem-serdes-default.go"
    ```

=== "C#"

    Not available. The single `ILambdaSerializer` you register at the host boundary is
    already applied to every durable operation result in the handler.

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

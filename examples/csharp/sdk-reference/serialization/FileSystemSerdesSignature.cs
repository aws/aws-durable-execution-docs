using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.Serialization.SystemTextJson;

// FileSystemSerializer implements both ILambdaSerializer and IDurableResultSerializer. It
// wraps an inner ILambdaSerializer that performs the actual value<->bytes conversion, so you
// control the on-the-wire format (JSON, compressed, custom).
public static class FileSystemSerdesSignature
{
    public static ILambdaSerializer Create() =>
        new FileSystemSerializer(
            inner: new DefaultLambdaJsonSerializer(),   // required: value <-> bytes
            basePath: "/mnt/efs",                       // required: durable mount point
            storageMode: FileSystemStorageMode.Always,  // optional, default Always
            pathEncoding: FileSystemPathEncoding.Uri);  // optional, default Uri

    // Omit the inner serializer to reuse the serializer registered at the host boundary
    // (the [assembly: LambdaSerializer(...)] serializer, or the one passed to
    // LambdaBootstrapBuilder.Create(handler, serializer)). The durable runtime binds it as
    // the inner when this instance runs through a per-operation serializer slot, so you do
    // not have to thread the global serializer in yourself.
    public static ILambdaSerializer CreateUsingGlobalSerializer() =>
        new FileSystemSerializer(
            basePath: "/mnt/efs",                       // required: durable mount point
            storageMode: FileSystemStorageMode.Always,  // optional, default Always
            pathEncoding: FileSystemPathEncoding.Uri);  // optional, default Uri
}

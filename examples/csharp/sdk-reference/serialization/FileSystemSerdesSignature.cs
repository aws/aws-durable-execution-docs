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
}

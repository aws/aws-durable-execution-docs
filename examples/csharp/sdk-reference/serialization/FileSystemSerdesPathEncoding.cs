using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.Serialization.SystemTextJson;

public static class FileSystemSerdesPathEncoding
{
    // URI (default): human-navigable paths built from the durable execution ARN
    // (function / execution / invocation), with the entity id as the file name.
    public static ILambdaSerializer Uri() =>
        new FileSystemSerializer(
            new DefaultLambdaJsonSerializer(), "/mnt/efs",
            pathEncoding: FileSystemPathEncoding.Uri);

    // HASH: the ARN (directory) and entity id (file name) are each replaced by their SHA-256
    // hex digest — fixed length and always filesystem-safe, regardless of length or charset.
    public static ILambdaSerializer Hash() =>
        new FileSystemSerializer(
            new DefaultLambdaJsonSerializer(), "/mnt/efs",
            pathEncoding: FileSystemPathEncoding.Hash);
}

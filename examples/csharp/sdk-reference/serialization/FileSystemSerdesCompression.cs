using System.IO.Compression;
using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.Serialization.SystemTextJson;

// The inner serializer controls the on-the-wire format. Wrap one to compress results before
// they are written to the filesystem — FileSystemSerializer just handles storage.
public sealed class GzipJsonSerializer : ILambdaSerializer
{
    private readonly ILambdaSerializer _inner = new DefaultLambdaJsonSerializer();

    public void Serialize<T>(T response, Stream responseStream)
    {
        using var gzip = new GZipStream(responseStream, CompressionLevel.Optimal, leaveOpen: true);
        _inner.Serialize(response, gzip);
    }

    public T Deserialize<T>(Stream requestStream)
    {
        using var gzip = new GZipStream(requestStream, CompressionMode.Decompress, leaveOpen: true);
        return _inner.Deserialize<T>(gzip);
    }
}

public static class FileSystemSerdesCompression
{
    // Results are JSON-encoded, gzip-compressed, then written to EFS.
    public static ILambdaSerializer Create() =>
        new FileSystemSerializer(inner: new GzipJsonSerializer(), basePath: "/mnt/efs");
}

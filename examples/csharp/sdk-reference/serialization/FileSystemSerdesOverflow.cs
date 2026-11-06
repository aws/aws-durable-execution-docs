using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.Serialization.SystemTextJson;

public static class FileSystemSerdesOverflow
{
    // ALWAYS (default): every result is written to a file; the checkpoint holds only a pointer.
    public static ILambdaSerializer Always() =>
        new FileSystemSerializer(
            new DefaultLambdaJsonSerializer(), "/mnt/efs", FileSystemStorageMode.Always);

    // OVERFLOW: the result stays inline in the checkpoint until it would exceed the durable
    // execution checkpoint size limit (~256 KB), then it spills to a file.
    public static ILambdaSerializer Overflow() =>
        new FileSystemSerializer(
            new DefaultLambdaJsonSerializer(), "/mnt/efs", FileSystemStorageMode.Overflow);
}

using System.Text.Json;
using Amazon.Lambda.Core;

// .NET has no per-operation SerDes. Serialization is controlled by the single
// ILambdaSerializer registered on ILambdaContext.Serializer. To customize how
// durable operation results are serialized, implement ILambdaSerializer and
// register it at the host boundary instead of passing a SerDes per operation.
public class OrderLambdaSerializer : ILambdaSerializer
{
    private static readonly JsonSerializerOptions Options = new()
    {
        PropertyNameCaseInsensitive = true,
    };

    public T Deserialize<T>(Stream requestStream)
    {
        using var reader = new StreamReader(requestStream);
        string json = reader.ReadToEnd();
        return JsonSerializer.Deserialize<T>(json, Options)!;
    }

    public void Serialize<T>(T response, Stream responseStream)
    {
        string json = JsonSerializer.Serialize(response, Options);
        using var writer = new StreamWriter(responseStream, leaveOpen: true);
        writer.Write(json);
        writer.Flush();
    }
}

using System.Text;
using Amazon.Lambda.Core;

// A pass-through serializer stores string values as-is (already a JSON string)
// instead of re-encoding them as a JSON string literal. .NET has no per-operation
// SerDes, so this behavior is expressed as a custom ILambdaSerializer registered
// on ILambdaContext.Serializer at the host boundary. It applies to every durable
// operation result in the handler.
public class PassThroughLambdaSerializer : ILambdaSerializer
{
    public T Deserialize<T>(Stream requestStream)
    {
        using var reader = new StreamReader(requestStream);
        string data = reader.ReadToEnd();
        // Return the raw payload unchanged when T is string.
        return (T)(object)data;
    }

    public void Serialize<T>(T response, Stream responseStream)
    {
        // Write the value as-is; the value is expected to already be a JSON string.
        string data = (string)(object)response!;
        var bytes = Encoding.UTF8.GetBytes(data);
        responseStream.Write(bytes, 0, bytes.Length);
    }
}

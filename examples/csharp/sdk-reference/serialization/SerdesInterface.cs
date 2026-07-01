// .NET has no per-operation SerDes interface. Serialization for every durable
// operation is handled by the single ILambdaSerializer registered on
// ILambdaContext.Serializer. Implement this interface to customize serialization.
public interface ILambdaSerializer
{
    T Deserialize<T>(Stream requestStream);

    void Serialize<T>(T response, Stream responseStream);
}

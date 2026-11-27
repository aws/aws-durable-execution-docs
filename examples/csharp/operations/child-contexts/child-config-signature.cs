public sealed class ChildContextConfig
{
    public string? SubType { get; set; }
    public Func<Exception, Exception>? ErrorMapping { get; set; }
    public NestingType NestingType { get; set; } = NestingType.Nested;
    public ILambdaSerializer? Serializer { get; set; }
}

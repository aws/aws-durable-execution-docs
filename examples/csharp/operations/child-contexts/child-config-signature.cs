public sealed class ChildContextConfig
{
    public string? SubType { get; set; }
    public Func<Exception, Exception>? ErrorMapping { get; set; }
}

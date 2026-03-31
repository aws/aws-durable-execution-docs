// DurableLogger wraps SLF4J Logger and adds MDC-based context enrichment.
// It implements these methods:

public class DurableLogger {
    public void trace(String format, Object... args) { ... }
    public void debug(String format, Object... args) { ... }
    public void info(String format, Object... args) { ... }
    public void warn(String format, Object... args) { ... }
    public void error(String format, Object... args) { ... }
    public void error(String message, Throwable t) { ... }
}

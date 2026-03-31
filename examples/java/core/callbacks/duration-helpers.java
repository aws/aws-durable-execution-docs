import java.time.Duration;
import software.amazon.lambda.durable.callback.CallbackConfig;

// Various ways to specify duration
Duration timeout60s = Duration.ofSeconds(60);
Duration timeout5m = Duration.ofMinutes(5);
Duration timeout2h = Duration.ofHours(2);
Duration timeout1d = Duration.ofDays(1);

// Use in CallbackConfig
var config = CallbackConfig.builder()
    .timeout(Duration.ofHours(2))
    .heartbeatTimeout(Duration.ofMinutes(15))
    .build();

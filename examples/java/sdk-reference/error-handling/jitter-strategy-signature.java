import software.amazon.lambda.durable.retry.JitterStrategy;

enum JitterStrategy {
    NONE, // exact calculated delay
    FULL, // random between 0 and base_delay
    HALF  // random between 50% and 100% of base_delay
}

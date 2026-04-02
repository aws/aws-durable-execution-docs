WaitForConditionWaitStrategy<Map<String, String>> strategy = (state, attempt) -> {
    if (attempt >= 10) {
        throw new WaitForConditionFailedException("Max attempts exceeded");
    }
    return Duration.ofSeconds(attempt * 5);
};

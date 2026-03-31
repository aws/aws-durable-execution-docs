// If execution times out, increase the timeout or check for long-running steps
var runner = LocalDurableTestRunner.create(Map.class, new MyHandler());
var result = runner.runUntilComplete(Map.of("input", "test"));

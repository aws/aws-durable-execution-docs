// Local mode: short timeout
var localRunner = LocalDurableTestRunner.create(String.class, new MyHandler());
var localResult = localRunner.runUntilComplete("test");

// Cloud mode: longer timeout (configured via CloudDurableTestRunner)
var cloudRunner = CloudDurableTestRunner.create(functionArn, String.class, String.class);
var cloudResult = cloudRunner.runUntilComplete("test");

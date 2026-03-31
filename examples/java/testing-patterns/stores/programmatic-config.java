// In-memory store (default) — used by LocalDurableTestRunner
var localRunner = LocalDurableTestRunner.create(Map.class, new MyHandler());

// Cloud runner — test against a deployed Lambda function
var cloudRunner = CloudDurableTestRunner.create(
    "arn:aws:lambda:us-west-2:123456789012:function:my-function:$LATEST",
    Map.class,
    Map.class
);

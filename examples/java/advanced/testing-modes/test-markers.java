// JUnit setup — create runner in @BeforeEach or directly in test methods

// Local mode: provide handler class
var localRunner = LocalDurableTestRunner.create(Map.class, new MyHandler());

// Cloud mode: provide Lambda function ARN
var cloudRunner = CloudDurableTestRunner.create(
    "arn:aws:lambda:us-east-1:123456789:function:my-function",
    Map.class,
    Map.class
);

var runner = CloudDurableTestRunner.create(
    "arn:aws:lambda:us-west-2:123456789012:function:my-function:$LATEST",
    Map.class,
    Map.class
);

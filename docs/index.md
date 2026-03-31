# AWS Durable Execution SDK Developer Guide

!!! note

    This guide covers the **AWS Durable Execution SDK**, the client library you use to write
    durable functions in TypeScript, Python, and Java. For service-level topics including
    IAM permissions, service quotas, infrastructure-as-code configuration, and monitoring,
    see
    [Lambda durable functions](https://docs.aws.amazon.com/lambda/latest/dg/durable-functions.html)
    in the Lambda Developer Guide.

The AWS Durable Execution SDK lets you build reliable, long-running workflows in AWS
Lambda. Your functions can pause execution, wait for external events, retry failed
operations, and resume exactly where they left off, even if Lambda recycles your
execution environment.

If you are new, [Get started](getting-started/) over here.

For detailed programming language reference, see [SDK Reference](sdk-reference/).

## Key features

- **Automatic checkpointing** Workflow state is saved automatically after each operation
- **Durable steps** Execute code with configurable retry strategies and at-most-once or
    at-least-once semantics
- **Wait operations** Pause execution for seconds, minutes, or hours without blocking
    Lambda resources
- **Callbacks** Wait for external systems to respond with results or approvals
- **Parallel execution** Run multiple operations concurrently with configurable
    completion criteria
- **Map operations** Process collections in parallel with batching and failure tolerance
- **Child contexts** Isolate nested workflows for better organization and error handling
- **Structured logging** Integrate with your logger to track execution flow and debug
    issues

## Use cases

- **Order processing workflows** Validate orders, charge payments, and fulfill shipments
    with automatic retry on failures.
- **Approval workflows** Wait for human approvals or external system responses using
    callbacks.
- **Data processing pipelines** Process large datasets in parallel with map operations
    and failure tolerance.
- **Multi-step integrations** Coordinate calls to multiple services with proper error
    handling and state management.
- **Long-running tasks** Execute workflows that take minutes or hours without blocking
    Lambda resources.
- **Saga patterns** Implement distributed transactions with compensation logic for
    failures.

## Getting help

- **Documentation** Use the navigation above to find specific topics.
- **Examples** Check the `examples/` directory in the repository for working code
    samples.
- **Issues** Report bugs or request features on the GitHub repositories:
    - :simple-github:
        [aws-durable-execution-sdk-js](https://github.com/aws/aws-durable-execution-sdk-js)
    - :simple-github:
        [aws-durable-execution-sdk-python](https://github.com/aws/aws-durable-execution-sdk-python)
    - :simple-github:
        [aws-durable-execution-sdk-java](https://github.com/aws/aws-durable-execution-sdk-java)

## Related documentation

The following topics are covered in
[Lambda service documentation for durable functions](https://docs.aws.amazon.com/lambda/latest/dg/durable-functions.html):

- [Service quotas & limits for durable functions](https://docs.aws.amazon.com/general/latest/gr/lambda-service.html)
- [IAM permissions & security for durable functions](https://docs.aws.amazon.com/lambda/latest/dg/durable-security.html)
- [Infrastructure-as-Code for durable functions: CDK, CloudFormation and SAM](https://docs.aws.amazon.com/lambda/latest/dg/durable-getting-started-iac.html)
- [Monitoring & debugging durable functions](https://docs.aws.amazon.com/lambda/latest/dg/durable-monitoring.html)
- [Durable functions vs Step Functions](https://docs.aws.amazon.com/lambda/latest/dg/durable-step-functions.html)
- [Invoking durable functions](https://docs.aws.amazon.com/lambda/latest/dg/durable-invoking.html)

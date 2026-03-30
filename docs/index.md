# AWS Durable Execution SDK

The AWS Durable Execution SDK lets you build reliable, long-running workflows in AWS Lambda. Your functions can pause execution, wait for external events, retry failed operations, and resume exactly where they left off—even if Lambda recycles your execution environment.

## Key features

- **Automatic checkpointing** - Your workflow state is saved automatically after each operation
- **Durable steps** - Execute code with configurable retry strategies and at-most-once or at-least-once semantics
- **Wait operations** - Pause execution for seconds, minutes, or hours without blocking Lambda resources
- **Callbacks** - Wait for external systems to respond with results or approvals
- **Parallel execution** - Run multiple operations concurrently with configurable completion criteria
- **Map operations** - Process collections in parallel with batching and failure tolerance
- **Child contexts** - Isolate nested workflows for better organization and error handling
- **Structured logging** - Integrate with your logger to track execution flow and debug issues

## Use cases

**Order processing workflows** - Validate orders, charge payments, and fulfill shipments with automatic retry on failures.

**Approval workflows** - Wait for human approvals or external system responses using callbacks.

**Data processing pipelines** - Process large datasets in parallel with map operations and failure tolerance.

**Multi-step integrations** - Coordinate calls to multiple services with proper error handling and state management.

**Long-running tasks** - Execute workflows that take minutes or hours without blocking Lambda resources.

**Saga patterns** - Implement distributed transactions with compensation logic for failures.

## Getting help

**Documentation** - Use the navigation above to find specific topics.

**Examples** - Check the `examples/` directory in the repository for working code samples.

**Issues** - Report bugs or request features on the [GitHub repository](https://github.com/awslabs/aws-durable-execution-sdk-python).

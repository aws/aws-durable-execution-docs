# AWS Lambda durable functions - Kiro Power

A Kiro power for building resilient, long-running multi-step applications and AI workflows with AWS Lambda durable functions. Durable functions automatically checkpoint progress, suspend execution for up to one year during long-running tasks, and recover from failures.

## What This Power Provides

- **Replay Model Guidance** - Critical rules to avoid non-deterministic bugs
- **Step Operations** - Atomic operations with retry strategies
- **Wait Operations** - Delays, callbacks, and polling patterns
- **Concurrent Operations** - Map and parallel execution with concurrency control
- **Error Handling** - Retry strategies, saga pattern, and compensating transactions
- **Testing Patterns** - Local and cloud testing with test runners
- **Deployment** - CloudFormation, CDK, and SAM templates

## Installation

### For Kiro CLI

1. Open Kiro → Powers panel
2. Click **Add power from Local Path**
3. Select this `power-aws-lambda-durable-functions` directory

### For Kiro IDE

Add to your workspace powers configuration.

## What You'll Learn

### Critical Concepts

- **Replay Model** - How Lambda durable functions execute and replay
- **Determinism Rules** - What must be inside steps vs outside
- **Qualified ARNs** - Why versions/aliases are required
- **Checkpoint Strategy** - When and how state is persisted

### Practical Patterns

- Multi-step workflows
- GenAI agentic loops
- Human-in-the-loop approvals
- Saga pattern for distributed transactions
- Batch processing with partial failure handling
- External system integration via callbacks

## Power Structure

```
power-aws-lambda-durable-functions/
├── POWER.md                          # Main power file
└── steering/                         # Workflow-specific guidance
    ├── getting-started.md            # Quick start and common patterns
    ├── replay-model-rules.md         # CRITICAL: Replay model constraints
    ├── step-operations.md            # Step patterns and retry strategies
    ├── wait-operations.md            # Waits, callbacks, and polling
    ├── concurrent-operations.md      # Map and parallel execution
    ├── error-handling.md             # Retry strategies and saga pattern
    ├── testing-patterns.md           # Local and cloud testing
    └── deployment-iac.md             # CloudFormation, CDK, SAM
```

## Keywords That Activate This Power

When you mention these keywords, Kiro will automatically load this power:

- lambda, durable, workflow, orchestration, state machine
- retry, checkpoint, long-running, stateful
- saga, agentic, ai workflow, human-in-the-loop, callback
- aws, serverless

## Quick Start

Once installed, try:

```
"Help me create a durable Lambda function that processes orders with retries"
```

Kiro will load the appropriate steering files and guide you through:
1. Setting up the handler with proper replay model rules
2. Implementing steps with retry strategies
3. Adding error handling and compensating transactions
4. Writing tests with LocalDurableTestRunner
5. Deploying with CloudFormation/CDK/SAM

## Contributing

To improve this power:

1. Add new patterns to existing steering files
2. Create new steering files for specialized workflows
3. Update POWER.md with new keyword mappings
4. Test with real-world scenarios

## Resources

- [AWS Lambda durable functions Documentation](https://docs.aws.amazon.com/lambda/latest/dg/durable-functions.html)
- [JavaScript SDK Repository](https://github.com/aws/aws-durable-execution-sdk-js)
- [Python SDK Repository](https://github.com/aws/aws-durable-execution-sdk-python)
- [Kiro Powers Documentation](https://kiro.dev/docs/powers/create/)

## License

This project is licensed under the Apache-2.0 License.
async wrapInvocation(
  info: InvocationInfo,
  fn: () => Promise<DurableExecutionInvocationOutput>,
): Promise<DurableExecutionInvocationOutput> {
  console.log(`wrap invocation, arn: ${info.executionArn}`);
  return await fn();
},

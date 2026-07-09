async onInvocationStart(info: InvocationInfo): Promise<void> {
  console.log(`invocation start, arn: ${info.executionArn}, first invocation: ${info.isFirstInvocation}`);
},

async onInvocationEnd(info: InvocationEndInfo): Promise<void> {
  console.log(`invocation end, arn: ${info.executionArn}, status: ${info.status}`);
},

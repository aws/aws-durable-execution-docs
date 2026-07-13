import {
  DurableInstrumentationPlugin,
  InvocationInfo,
  InvocationEndInfo,
  OperationInfo,
  OperationEndInfo,
  AttemptInfo,
  AttemptEndInfo,
  OperationChangeInfo,
  DurableExecutionInvocationOutput,
} from "@aws/durable-execution-sdk-js";

export const examplePlugin: DurableInstrumentationPlugin = {
  async onInvocationStart(info: InvocationInfo): Promise<void> {
    console.log(`invocation start, arn: ${info.executionArn}, first invocation: ${info.isFirstInvocation}`);
  },

  async onInvocationEnd(info: InvocationEndInfo): Promise<void> {
    console.log(`invocation end, arn: ${info.executionArn}, status: ${info.status}`);
  },

  async onOperationStart(info: OperationInfo): Promise<void> {
    console.log(`operation ${info.name} start, type: ${info.type}, replay: ${info.isReplay}`);
  },

  async onOperationEnd(info: OperationEndInfo): Promise<void> {
    console.log(`operation ${info.name} end, status: ${info.status}, error: ${info.error}`);
  },

  async onOperationChange(info: OperationChangeInfo): Promise<void> {
    console.log(`operations changed, ids: ${Object.keys(info.updatedOperations)}`);
  },

  async onOperationAttemptStart(info: AttemptInfo): Promise<void> {
    console.log(`attempt ${info.name} start, attempt: ${info.attempt}`);
  },

  async onOperationAttemptEnd(info: AttemptEndInfo): Promise<void> {
    console.log(`attempt ${info.name} end, attempt: ${info.attempt}, outcome: ${info.outcome}`);
  },

  async wrapInvocation(
    info: InvocationInfo,
    fn: () => Promise<DurableExecutionInvocationOutput>,
  ): Promise<DurableExecutionInvocationOutput> {
    console.log(`wrap invocation, arn: ${info.executionArn}`);
    return await fn();
  },

  wrapChildContextFn(info: OperationInfo, fn: () => unknown): unknown {
    console.log(`wrap child context ${info.name}`);
    return fn();
  },

  wrapOperationAttemptFn(info: AttemptInfo, fn: () => unknown): unknown {
    console.log(`wrap attempt ${info.name}, attempt: ${info.attempt}`);
    return fn();
  },

  enrichLogContext(): Record<string, string | number | boolean> {
    return { service: "orders" };
  },
};

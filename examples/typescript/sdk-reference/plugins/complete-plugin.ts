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
    console.log("invocation start", info.executionArn, info.isFirstInvocation);
  },

  async onInvocationEnd(info: InvocationEndInfo): Promise<void> {
    console.log("invocation end", info.status);
  },

  async onOperationStart(info: OperationInfo): Promise<void> {
    console.log("operation start", info.type, info.name, info.isReplay);
  },

  async onOperationEnd(info: OperationEndInfo): Promise<void> {
    console.log("operation end", info.name, info.status, info.error);
  },

  async onOperationAttemptStart(info: AttemptInfo): Promise<void> {
    console.log("attempt start", info.name, info.attempt);
  },

  async onOperationAttemptEnd(info: AttemptEndInfo): Promise<void> {
    console.log("attempt end", info.name, info.attempt, info.outcome);
  },

  async wrapInvocation(
    info: InvocationInfo,
    fn: () => Promise<DurableExecutionInvocationOutput>,
  ): Promise<DurableExecutionInvocationOutput> {
    console.log("wrap invocation", info.executionArn);
    return await fn();
  },

  wrapChildContextFn(info: OperationInfo, fn: () => unknown): unknown {
    console.log("wrap child context", info.name);
    return fn();
  },

  wrapOperationAttemptFn(info: AttemptInfo, fn: () => unknown): unknown {
    console.log("wrap attempt", info.name, info.attempt);
    return fn();
  },

  async onOperationChange(info: OperationChangeInfo): Promise<void> {
    console.log("operations changed", Object.keys(info.updatedOperations));
  },

  enrichLogContext(): Record<string, string | number | boolean> {
    return { service: "orders" };
  },
};

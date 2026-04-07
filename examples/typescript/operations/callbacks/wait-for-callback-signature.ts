// Named overload
waitForCallback<TOutput = string>(
  name: string | undefined,
  submitter: (callbackId: string, context: WaitForCallbackContext) => Promise<void>,
  config?: WaitForCallbackConfig<TOutput>,
): DurablePromise<TOutput>;

// Anonymous overload
waitForCallback<TOutput = string>(
  submitter: (callbackId: string, context: WaitForCallbackContext) => Promise<void>,
  config?: WaitForCallbackConfig<TOutput>,
): DurablePromise<TOutput>;

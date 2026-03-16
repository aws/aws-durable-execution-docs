import { MapConfig, CompletionConfig } from '@aws/durable-execution-sdk-js';

const items = Array.from({ length: 20 }, (_, i) => i);

const results = await context.map(
  'process-items',
  items,
  async (ctx, item, index) => {
    // Processing that might fail
    if (item % 7 === 0) {
      throw new Error(`Item ${item} failed`);
    }
    return { item, processed: true };
  },
  {
    completionConfig: {
      minSuccessful: 15,           // Succeed if at least 15 items succeed
      toleratedFailureCount: 5,    // Fail after 5 failures
    }
  }
);

return results.getResults();

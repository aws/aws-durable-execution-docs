# Replay Model Rules - CRITICAL

The replay model is the foundation of durable functions. Violations cause subtle, hard-to-debug issues. **Read this carefully.**

## How Replay Works

Durable functions use a "checkpoint and replay" execution model:

1. Code runs from the beginning on every invocation
2. Steps that already completed return their checkpointed results WITHOUT re-executing
3. Code OUTSIDE steps executes again on every replay
4. New steps execute when reached

**Example:**

```typescript
// First execution: Runs lines 1-5
// After wait: Runs lines 1-5 again (line 2 returns cached result)
const data = await context.step('fetch', async () => fetchAPI());  // Line 2: Executes once, cached
await context.wait({ seconds: 60 });                                // Line 3: Waits
const result = await context.step('process', async () => process(data)); // Line 5: Executes after wait
```

## Rule 1: Deterministic Code Outside Durable Operations

**ALL code outside durable operations MUST produce the same result on every replay.**

### ❌ WRONG - Non-Deterministic Outside Durable Operations

**TypeScript:**

```typescript
// These values change on each replay!
const id = uuid.v4();                    // Different UUID each time
const timestamp = Date.now();            // Different timestamp each time
const random = Math.random();            // Different random number
const now = new Date();                  // Different date each time

await context.step('save', async () => saveData({ id, timestamp }));
```

**Python:**

```python
# These values change on each replay!
id = str(uuid.uuid4())                   # Different UUID each time
timestamp = time.time()                  # Different timestamp each time
random_val = random.random()             # Different random number
now = datetime.now()                     # Different datetime each time

context.step(lambda _: save_data({"id": id}), name='save')
```

### ✅ CORRECT - Non-Deterministic Inside Durable Operations

**TypeScript:**

```typescript
const id = await context.step('generate-id', async () => uuid.v4());
const timestamp = await context.step('get-time', async () => Date.now());
const random = await context.step('random', async () => Math.random());
const now = await context.step('get-date', async () => new Date());

await context.step('save', async () => saveData({ id, timestamp }));
```

**Python:**

```python
id = context.step(lambda _: str(uuid.uuid4()), name='generate-id')
timestamp = context.step(lambda _: time.time(), name='get-time')
random_val = context.step(lambda _: random.random(), name='random')
now = context.step(lambda _: datetime.now(), name='get-date')

context.step(lambda _: save_data({"id": id}), name='save')
```

### Must Be In Durable Operations

- `Date.now()`, `new Date()`, `time.time()`, `datetime.now()`
- `Math.random()`, `random.random()`
- UUID generation (`uuid.v4()`, `uuid.uuid4()`)
- API calls, HTTP requests
- Database queries
- File system operations
- Environment variable reads (if they can change)
- Any external system interaction

Durable operations include `context.step(...)`, `waitForCallback(...)`, `waitForCondition(...)`, and branch/item functions passed to `context.parallel(...)` and `context.map(...)`.

## Rule 2: Durable Operation Bodies Are Not Guaranteed To Be Atomic

**Functions passed to durable context APIs must assume the operation is not guaranteed to be atomic with respect to external side effects, and may be re-attempted before the durable runtime has fully recorded the result.**

This rule applies to:

- `context.step(...)`
- `waitForCallback(...)` submitters
- `waitForCondition(...)` check functions
- Branch/item functions used by `context.parallel(...)` and `context.map(...)`

### What This Means

- Non-deterministic computation inside a durable operation body is acceptable because the result can be checkpointed
- External side effects started from that body should still be safe under re-attempt whenever possible
- If the side effect needs an identifier for idempotency, derive it from durable inputs/state or generate it once from durable state and reuse it
- If a **step** cannot be made idempotent and duplicate execution is unacceptable, use `StepSemantics.AtMostOncePerRetry` (TypeScript) or `StepSemantics.AT_MOST_ONCE_PER_RETRY` (Python) with retries disabled so the behavior is effectively zero-or-once rather than more than once

### ❌ WRONG - Unstable External Identity Inside Durable Operation Body

**TypeScript:**

```typescript
await context.step('start-export', async () => {
  const jobId = `export-${Date.now()}`;
  await exportClient.start({ jobId, orderId });
});
```

**Python:**

```python
context.step(
    lambda _: export_client.start({
        'job_id': f'export-{time.time()}',
        'order_id': order_id
    }),
    name='start-export'
)
```

### ✅ CORRECT - Stable Identity Derived From Durable State

**TypeScript:**

```typescript
const jobId = `export-${orderId}`;

await context.step('start-export', async () => {
  await exportClient.start({ jobId, orderId });
});
```

**Python:**

```python
job_id = f'export-{order_id}'

context.step(
    lambda _: export_client.start({
        'job_id': job_id,
        'order_id': order_id
    }),
    name='start-export'
)
```

## Rule 3: No Nested Durable Operations

**You CANNOT call durable operations inside a step function.**

### ❌ WRONG - Nested Operations

**TypeScript:**

```typescript
await context.step('process', async () => {
  await context.wait({ seconds: 1 });      // ERROR!
  await context.step(async () => ...);     // ERROR!
  await context.invoke('other-fn', ...);   // ERROR!
  return result;
});
```

**Python:**

```python
@durable_step
def process(step_ctx: StepContext):
    context.wait(duration=Duration.from_seconds(1))  # ERROR!
    context.step(lambda _: ..., name='nested')       # ERROR!
    return result

context.step(process())
```

### ✅ CORRECT - Use Child Context

**TypeScript:**

```typescript
await context.runInChildContext('process', async (childCtx) => {
  await childCtx.wait({ seconds: 1 });
  const step1 = await childCtx.step('validate', async () => validate());
  const step2 = await childCtx.step('process', async () => process(step1));
  return step2;
});
```

**Python:**

```python
# Note: validate and process are decorated with @durable_step
def process_child(child_ctx: DurableContext):
    child_ctx.wait(duration=Duration.from_seconds(1))
    step1 = child_ctx.step(validate())
    step2 = child_ctx.step(process(step1))
    return step2

context.run_in_child_context(func=process_child, name='process')
```

## Rule 4: Closure Mutations Are Lost

**Variables mutated inside steps are NOT preserved across replays.**

### ❌ WRONG - Lost Mutations

**TypeScript:**

```typescript
let counter = 0;
await context.step('increment', async () => {
  counter++;  // This mutation is lost!
});
console.log(counter);  // Always 0 on replay!
```

**Python:**

```python
counter = 0
@durable_step
def increment(step_ctx: StepContext):
    nonlocal counter
    counter += 1  # This mutation is lost!

context.step(increment())
print(counter)  # Always 0 on replay!
```

### ✅ CORRECT - Return Values

**TypeScript:**

```typescript
let counter = 0;
counter = await context.step('increment', async () => counter + 1);
console.log(counter);  // Correct value
```

**Python:**

```python
counter = 0
counter = context.step(lambda _: counter + 1, name='increment')
print(counter)  # Correct value
```

## Rule 5: Side Effects Outside Durable Operations Repeat

**Side effects outside durable operations happen on EVERY replay.**

### ❌ WRONG - Repeated Side Effects

**TypeScript:**

```typescript
console.log('Starting process');     // Logs multiple times!
await sendEmail(user.email);         // Sends multiple emails!
await updateDatabase(data);          // Updates multiple times!

await context.step('process', async () => process());
```

**Python:**

```python
print('Starting process')            # Prints multiple times!
send_email(user.email)               # Sends multiple emails!
update_database(data)                # Updates multiple times!

context.step(lambda _: process(), name='process')
```

### ✅ CORRECT - Replay-Aware Logging And Checkpointed Side Effects

**TypeScript:**

```typescript
context.logger.info('Starting process');  // Deduplicated automatically
await context.step('send-email', async () => sendEmail(user.email));
await context.step('update-db', async () => updateDatabase(data));
await context.step('process', async () => process());
```

**Python:**

```python
# Note: Functions are decorated with @durable_step
context.logger.info('Starting process')  # Deduplicated automatically
context.step(send_email(user.email))
context.step(update_database(data))
context.step(process())
```

### Exception: context.logger

`context.logger` is replay-aware and safe to use anywhere. It automatically deduplicates logs across replays.

Custom loggers are still allowed. If you use a non-replay-aware logger outside durable operations, expect duplicate log entries on replay. If you want to keep an existing logging interface, configure `context.logger` to wrap that existing logger inside the durable handler.

## Common Pitfalls

### Pitfall 1: Reading Environment Variables

```typescript
// ❌ WRONG if env vars can change
const apiKey = process.env.API_KEY;
await context.step('call-api', async () => callAPI(apiKey));

// ✅ CORRECT
const apiKey = await context.step('get-key', async () => process.env.API_KEY);
await context.step('call-api', async () => callAPI(apiKey));
```

### Pitfall 2: Array/Object Mutations

```typescript
// ❌ WRONG
const items = [];
await context.step('add-item', async () => {
  items.push(newItem);  // Lost on replay
});

// ✅ CORRECT
let items = [];
items = await context.step('add-item', async () => [...items, newItem]);
```

### Pitfall 3: Conditional Logic with Non-Deterministic Values

```typescript
// ❌ WRONG
if (Math.random() > 0.5) {  // Different on each replay!
  await context.step('path-a', async () => ...);
} else {
  await context.step('path-b', async () => ...);
}

// ✅ CORRECT
const shouldTakePathA = await context.step('decide', async () => Math.random() > 0.5);
if (shouldTakePathA) {
  await context.step('path-a', async () => ...);
} else {
  await context.step('path-b', async () => ...);
}
```

### Pitfall 4: Assuming Durable Operation Bodies Are Atomic

```typescript
// ❌ WRONG
await context.waitForCallback(
  'wait-payment',
  async (callbackId) => {
    const requestId = `payment-${Date.now()}`;
    await paymentProvider.createPayment({ requestId, callbackId });
  }
);

// ✅ CORRECT
const requestId = `payment-${orderId}`;
await context.waitForCallback(
  'wait-payment',
  async (callbackId) => {
    await paymentProvider.createPayment({ requestId, callbackId });
  }
);
```

## Debugging Replay Issues

If you see inconsistent behavior:

1. **Check for non-deterministic code outside durable operations**
2. **Check durable operation bodies for non-atomic external side effects**
3. **Verify no nested durable operations**
4. **Look for closure mutations**
5. **Search for side effects outside durable operations**
6. **Use `context.logger` to trace execution flow**

## Testing Replay Behavior

Always test with multiple invocations to simulate replay:

```typescript
const runner = new LocalDurableTestRunner({ handlerFunction: handler });
const execution = await runner.run({ payload: { test: true } });

// Verify operations executed correctly
const step1 = runner.getOperation('step-name');
expect(step1.getStatus()).toBe(OperationStatus.SUCCEEDED);
```

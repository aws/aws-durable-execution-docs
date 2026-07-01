export const meta = {
  name: 'add-csharp-tabs-apiref',
  description: 'Add C# tabs to the testing API-reference page (inline snippets, testing SDK)',
  phases: [
    { title: 'Author', detail: 'one agent authors all 39 C# tabs on the single page' },
    { title: 'Verify', detail: 'independent adversarial check of every C# snippet against testing-SDK source' },
  ],
}

const REPO = 'C:/dev/repos/aws-durable-execution-docs'
const TESTING_SRC = 'C:/dev/repos/aws-lambda-dotnet/Libraries/src/Amazon.Lambda.DurableExecution.Testing'
const MD = 'docs/testing/api-reference.md'

const API = `
VERIFIED .NET testing-SDK surface (read these files in ${TESTING_SRC} to confirm before writing):

DurableTestRunner<TInput, TOutput>  (DurableTestRunner.cs) — the LOCAL runner. Note the name is
  DurableTestRunner, NOT "LocalDurableTestRunner" (that is the Java/TS name). Members:
  - ctor: DurableTestRunner(Func<TInput, IDurableContext, Task<TOutput>> handler, TestRunnerOptions? options = null)
  - RegisterFunction<TPayload,TResult>(string functionNameOrArn, Func<TPayload, ILambdaContext, Task<TResult>> ...) -> runner (chainable)
  - RegisterDurableFunction<TPayload,TResult>(...) -> runner (chainable)
  - Task<TestResult<TOutput>> RunAsync(TInput input, TimeSpan? timeout = null, CancellationToken = default)
  - Task<string> StartAsync(TInput input, TimeSpan? timeout = null, CancellationToken = default)
  - Task<string> WaitForCallbackAsync(string durableExecutionArn, string? name = null, TimeSpan? timeout = null, CancellationToken = default)
  - Task SendCallbackSuccessAsync<TResult>(string callbackId, TResult result, CancellationToken = default)
  - Task SendCallbackFailureAsync(string callbackId, ErrorObject? error = null, CancellationToken = default)
  - Task SendCallbackHeartbeatAsync(string callbackId, CancellationToken = default)
  - Task<TestResult<TOutput>> WaitForResultAsync(string durableExecutionArn, TimeSpan? timeout = null, CancellationToken = default)
  - ValueTask DisposeAsync()   (use "await using var runner = ...")

TestRunnerOptions  (TestRunnerOptions.cs, a record): SkipTime (bool, default true), MaxInvocations
  (int, default 100), DefaultTimeout (TimeSpan, default 30s), Serializer (ILambdaSerializer?),
  LoggerFactory (ILoggerFactory?), DurableExecutionArn (string, default synthetic).

TestResult<TOutput>  (TestResult.cs): Status (InvocationStatus), IsSucceeded, IsFailed, Result (TOutput?),
  Error (ErrorObject?), DurableExecutionArn (string), InvocationCount (int?), Steps (IReadOnlyList<TestStep>),
  GetStep(name)->TestStep, FindStep(name)->TestStep?, GetSteps(name)->list, GetStepById(id)->TestStep,
  GetChildren(TestStep)->list, GetStepsByStatus(string status)->list, EnsureSucceeded().
  NOTE: InvocationStatus is {Succeeded, Failed, Pending} (from the core SDK). There is NO separate
  "ExecutionStatus" enum in .NET.

TestStep  (TestStep.cs): Id, Name (string?), ParentId (string?), Kind (OperationKind), SubKind (string?),
  Status (string — compare against OperationStatus constants), Attempt (int), StartedAt/EndedAt
  (DateTimeOffset?), Duration (TimeSpan?), Children (IReadOnlyList<TestStep>), GetResult<T>()->T?,
  GetError()->ErrorObject?, GetWaitEndsAt()->DateTimeOffset?, GetCallbackId()->string?,
  GetChainedInvokeFunctionName()->string?. The docs "Operation" type maps to TestStep in .NET.

OperationKind  (OperationKind.cs, enum): Step, Wait, Callback, ChainedInvoke, Context, Execution.
OperationStatus  (OperationStatus.cs, static string-const class): Started, Succeeded, Failed, Pending,
  TimedOut, Cancelled, Stopped, Ready.

CloudDurableTestRunner<TInput, TOutput>  (CloudDurableTestRunner.cs) — the CLOUD runner:
  - ctor: CloudDurableTestRunner(string functionName, CloudTestRunnerOptions? options = null, IAmazonLambda? lambdaClient = null)
    (READ the ctor to confirm exact parameters before writing.)
  - Same RunAsync/StartAsync/WaitForCallbackAsync/SendCallback*/WaitForResultAsync/DisposeAsync shape as local.
CloudTestRunnerOptions  (CloudTestRunnerOptions.cs, record): InitialPollInterval (TimeSpan, 200ms),
  PollInterval (TimeSpan, 2s), DefaultTimeout (TimeSpan, 5min), Serializer (ILambdaSerializer?).

CONCEPTS ON THIS PAGE THAT MAY NOT EXIST IN .NET — do NOT fabricate. Verify each against source;
if there is no .NET equivalent, write a short honest C# tab saying the .NET SDK does not expose it
and pointing to the nearest real API, rather than inventing a member:
  - "setupTestEnvironment / teardownTestEnvironment" and "LocalDurableTestRunnerSetupParameters"
    (TS-specific lifecycle) — .NET uses "await using" + the ctor; no static setup/teardown.
  - "History events" and "Invocations" sections — check whether TestResult exposes these. It exposes
    InvocationCount and Steps; if there is no history-events API, say so.
  - "Waiting operation status" enum — check for a .NET equivalent; OperationStatus covers statuses.
  - "Register mock handlers for invoke" — maps to RegisterFunction / RegisterDurableFunction.
  - "CloudDurableTestRunnerConfig" — the .NET type is CloudTestRunnerOptions.
`

const AUTHOR_SCHEMA = {
  type: 'object', additionalProperties: false,
  required: ['tabGroupsTotal', 'tabGroupsWithCsharp', 'compilableSnippets', 'compiledOk', 'divergences', 'notes'],
  properties: {
    tabGroupsTotal: { type: 'integer' },
    tabGroupsWithCsharp: { type: 'integer' },
    compilableSnippets: { type: 'integer', description: 'how many C# snippets were standalone-compilable and thus compile-checked' },
    compiledOk: { type: 'boolean', description: 'true only if every compile-checked snippet built green' },
    divergences: { type: 'array', items: { type: 'string' }, description: 'sections where .NET has no equivalent and the C# tab says so instead of inventing API' },
    notes: { type: 'string' },
  },
}

const VERDICT_SCHEMA = {
  type: 'object', additionalProperties: false,
  required: ['tabsComplete', 'noFabricatedApi', 'compileClean', 'issues', 'verdict'],
  properties: {
    tabsComplete: { type: 'boolean', description: 'every tab group ends with 4 tabs in order TS, Python, Java, C#' },
    noFabricatedApi: { type: 'boolean', description: 'every C# type/member used actually exists in the testing-SDK source' },
    compileClean: { type: 'boolean', description: 'every standalone-compilable C# snippet builds green' },
    issues: { type: 'array', items: { type: 'string' } },
    verdict: { type: 'string', enum: ['PASS', 'FAIL'] },
  },
}

phase('Author')

const author = await agent(
  `You are extending an AWS documentation page that shows the durable-execution TESTING API in
content tabs ordered TypeScript, Python, Java. Add a fourth "C#" tab to EVERY tab group on the
page. The page uses INLINE code + prose in each tab (NOT --8<-- includes), so you edit the
markdown directly; you do not create example files.

PAGE: ${MD}   (edit in place)
It has 39 "=== \\"Java\\"" tab groups across these sections: LocalDurableTestRunner (Create a
runner, Run the handler, Run asynchronously, Inspect operations, Drive callbacks, Drive chained
invokes, Register mock handlers for invoke, Simulate failures, Control time, Reset between runs,
Reference types), TestResult (Status, Result, Error, Operations, History events, Invocations,
Pretty-print, Reference types), Operation (Common accessors, Step/Wait/Callback/Chained invoke/
Context/Execution details, Drive a callback from an operation), Enums (Execution/Operation/
Operation type/Waiting operation status), CloudDurableTestRunner (Create a runner, Run the handler,
Run asynchronously, Inspect operations, Drive callbacks, Configure polling and timeouts, Reset
between runs, Reference types).

HARD RULES:
- Tab order ALWAYS TypeScript, Python, Java, C#. Add the C# block LAST in each group, right after Java,
  matching the Java block's indentation and mix of code + explanatory prose.
- Match the Java tab's FORMAT per group: if Java shows a code signature, show the C# signature; if Java
  shows prose + a snippet, mirror that.
- VERIFY EVERY MEMBER against source before writing. Read the files under ${TESTING_SRC}.
- DO NOT FABRICATE. Where a documented concept has no .NET equivalent, write a brief honest C# tab
  ("The .NET SDK does not expose X; use Y instead.") and record it in divergences. A truthful "no
  equivalent" note is REQUIRED over an invented member.
- The .NET local runner type is DurableTestRunner<TIn,TOut> (not LocalDurableTestRunner). Use
  "await using var runner = new DurableTestRunner<...>(workflow, new TestRunnerOptions { SkipTime = true });".
  Never invent members like WorkflowForTest — pass the workflow delegate or an inline async (input,ctx)=>... .

${API}

GROUND TRUTH TO IMITATE: the committed C# testing examples
  ${REPO}/examples/csharp/testing/authoring/*.cs and .../testing/examples/*.cs
show the exact idiom (runner ctor, RunAsync, TestResult/TestStep/OperationKind/OperationStatus usage).
Read a few before writing.

COMPILE-CHECK: for any C# snippet that is a COMPLETE compilable unit (a full [Fact] test or a class),
write it to a temp file and build it to confirm, from ${REPO}:
  dotnet build scripts/csharp-verify/dll-verify-testing.csproj -p:ExampleFile="<abs temp .cs>" -p:BaseIntermediateOutputPath=".verify/apiref-<n>/obj/" -p:BaseOutputPath=".verify/apiref-<n>/bin/" -v q --nologo
(unique .verify dir per snippet, forward slashes). Single-line signatures / interface listings / enum
listings are pseudo-signatures — do not compile those. Fix any snippet that fails to compile.

Return the manifest. tabGroupsWithCsharp must equal tabGroupsTotal (39).`,
  { label: 'author:api-reference', phase: 'Author', schema: AUTHOR_SCHEMA, effort: 'high' },
)

phase('Verify')

const verdict = author == null ? null : await agent(
  `Independently and adversarially VERIFY the C# tabs just added to ${MD}. Assume the author invented
API and mis-ordered tabs.

SOURCE: ${TESTING_SRC}  (DurableTestRunner.cs, CloudDurableTestRunner.cs, TestRunnerOptions.cs,
CloudTestRunnerOptions.cs, TestResult.cs, TestStep.cs, OperationKind.cs, OperationStatus.cs)
AUTHOR MANIFEST: ${JSON.stringify(author)}

Checks:
1. TABS: read ${MD}. Every content-tab group must end with exactly 4 tabs in order TypeScript, Python,
   Java, C#. Report any group with != 4 tabs or wrong order (quote the nearest heading). tabsComplete.
2. NO FABRICATED API: for EVERY C# type and member referenced in the new tabs, confirm it exists in the
   source with that exact name/signature (DurableTestRunner, CloudDurableTestRunner, TestResult, TestStep,
   OperationKind, OperationStatus, TestRunnerOptions, CloudTestRunnerOptions, and their members). Any
   invented member (e.g. WorkflowForTest, LocalDurableTestRunner, a non-existent enum) is a CONFIRMED
   issue. Honest "no .NET equivalent" notes are acceptable and NOT issues. noFabricatedApi reflects this.
3. COMPILE: for each C# snippet that is a complete compilable unit, write it to a temp file and build via
   scripts/csharp-verify/dll-verify-testing.csproj with fresh unique .verify dirs (forward slashes).
   compileClean = all such snippets build. Report failures with errors. Do not compile pseudo-signatures.

verdict = PASS only if tabsComplete && noFabricatedApi && compileClean.`,
  { label: 'verify:api-reference', phase: 'Verify', schema: VERDICT_SCHEMA, effort: 'high' },
)

return {
  page: MD,
  author,
  verdict,
  passed: verdict?.verdict === 'PASS',
}

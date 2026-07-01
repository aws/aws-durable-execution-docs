export const meta = {
  name: 'add-csharp-tabs-batch2',
  description: 'Add verified C# tabs to getting-started, patterns, and testing pages',
  phases: [
    { title: 'Author', detail: 'one agent per page: write C# examples + add === "C#" tabs, self-compile' },
    { title: 'Verify', detail: 'independent agent per page: recompile + check each .cs against SDK source' },
  ],
}

const REPO = 'C:/dev/repos/aws-durable-execution-docs'
const SDK_SRC = 'C:/dev/repos/aws-lambda-dotnet/Libraries/src/Amazon.Lambda.DurableExecution'
const TESTING_SRC = 'C:/dev/repos/aws-lambda-dotnet/Libraries/src/Amazon.Lambda.DurableExecution.Testing'

// kind: 'main' = uses the core SDK (dll-verify.csproj); 'testing' = uses the testing SDK
// (dll-verify-testing.csproj, references Amazon.Lambda.DurableExecution.Testing + xunit).
const PAGES = [
  {
    slug: 'key-concepts', md: 'docs/getting-started/key-concepts.md', kind: 'main',
    bases: ['getting-started/durable-context', 'getting-started/execution-model'],
    note: 'Conceptual page. C# examples mirror the TS/Py/Java shape: an envelope Handler delegating to DurableFunction.WrapAsync<TIn,TOut>, and a Workflow(TIn, IDurableContext ctx) method. durable-context shows accessing ctx (Logger, ExecutionContext); execution-model shows a small multi-step workflow.',
  },
  {
    slug: 'quickstart', md: 'docs/getting-started/quickstart.md', kind: 'main',
    bases: ['getting-started/quickstart'],
    note: 'DEPLOY WALKTHROUGH — high divergence. .NET packaging differs structurally. The C# quickstart should show the executable programming model (Main + LambdaBootstrap + HandlerWrapper + DefaultLambdaJsonSerializer) on the dotnet10 runtime, OR the class-library model, matching what the existing docs/sdk-reference/languages/csharp/index.md already documents. Reuse that page\'s verified quickstart code as the source of truth. Prose in the C# tab may legitimately differ from other languages (link to languages/csharp for full setup). Do not invent CLI/deploy commands — keep the C# tab to the code + a pointer to the C# language guide.',
  },
  {
    slug: 'quickstart-container', md: 'docs/getting-started/quickstart-container-image.md', kind: 'main',
    bases: ['getting-started/quickstart'],
    note: 'DEPLOY WALKTHROUGH (container image) — high divergence. The C# tab reuses the same quickstart workflow code as getting-started/quickstart. Do NOT fabricate a Dockerfile or base-image tags; if the surrounding prose shows container build steps per language and you cannot verify the .NET container flow from a real source, keep the C# tab to the workflow code plus a pointer to the C# language guide, and note in the manifest that container-deploy prose was not fabricated.',
  },
  {
    slug: 'code-organization', md: 'docs/patterns/best-practices/code-organization.md', kind: 'main',
    bases: ['patterns/code-organization/child-context', 'patterns/code-organization/group-config', 'patterns/code-organization/parallelism', 'patterns/code-organization/separate-logic'],
    note: 'Mirror the TS/Py/Java examples using RunInChildContextAsync, ParallelAsync, and plain method extraction. Verify signatures against IDurableContext.cs.',
  },
  {
    slug: 'determinism', md: 'docs/patterns/best-practices/determinism.md', kind: 'main',
    bases: ['patterns/determinism/non-deterministic-in-step', 'patterns/determinism/return-value-passing', 'patterns/determinism/stable-branches'],
    note: 'non-deterministic-in-step: wrap DateTime.UtcNow/Guid.NewGuid/random in a StepAsync. return-value-passing: return from step, do not mutate clos: mirror examples/csharp/operations/steps/passing-data-*.cs. stable-branches: keep branch structure deterministic across replays.',
  },
  {
    slug: 'idempotency', md: 'docs/patterns/best-practices/idempotency.md', kind: 'main',
    bases: ['patterns/idempotency/choose-semantics', 'patterns/idempotency/idempotency-tokens'],
    note: 'choose-semantics: StepConfig { Semantics = StepSemantics.AtMostOncePerRetry } for non-idempotent ops, AtLeastOncePerRetry (default) otherwise. Verify against StepConfig.cs / RetryStrategy.cs. idempotency-tokens: derive a stable token (e.g. from IStepContext.OperationId) and pass to the downstream call.',
  },
  {
    slug: 'pause-resume', md: 'docs/patterns/best-practices/pause-resume.md', kind: 'main',
    bases: ['patterns/pause-resume/callback-timeout', 'patterns/pause-resume/heartbeat-timeout', 'patterns/pause-resume/wait-for-condition', 'patterns/pause-resume/wait-vs-sleep'],
    note: 'callback-timeout/heartbeat-timeout: CallbackConfig / WaitForCallbackConfig timeout options (read CallbackConfig.cs, WaitForCallbackConfig.cs). wait-for-condition: WaitForConditionAsync<TState> with a WaitForConditionConfig<TState> (read WaitForConditionConfig.cs, IWaitStrategy.cs). wait-vs-sleep: ctx.WaitAsync(TimeSpan) vs Thread.Sleep/Task.Delay (the point: use WaitAsync so no compute charge).',
  },
  {
    slug: 'state', md: 'docs/patterns/best-practices/state.md', kind: 'main',
    bases: ['patterns/state/batch-result-pointers', 'patterns/state/durable-vs-local', 'patterns/state/store-references'],
    note: 'batch-result-pointers: work with IBatchResult<T> from Parallel/Map (read IBatchResult.cs, IBatchItem.cs). durable-vs-local: durable state comes from step return values, not local fields (mutations lost on replay). store-references: checkpoint a reference/key, not large blobs.',
  },
  {
    slug: 'step-design', md: 'docs/patterns/best-practices/step-design.md', kind: 'main',
    bases: ['patterns/step-design/handle-errors-in-step', 'patterns/step-design/one-thing-per-step', 'patterns/step-design/reusable-step', 'patterns/step-design/step-boundary', 'patterns/step-design/step-names'],
    note: 'Mirror TS/Py/Java. handle-errors-in-step: try/catch inside the step body or rely on RetryStrategy. reusable-step: extract a method returning Task<T> used as the step body. step-names: the name arg to StepAsync (optional in .NET; inferred from call site when omitted).',
  },
  {
    slug: 'testing-assertions', md: 'docs/testing/assertions.md', kind: 'testing',
    bases: ['testing/assertions/assert-callback', 'testing/assertions/assert-child-context', 'testing/assertions/assert-step', 'testing/assertions/assert-wait', 'testing/assertions/filter-by-status'],
    note: 'Use DurableTestRunner<TIn,TOut> + TestResult<T> + TestStep + OperationKind + OperationStatus (all in Amazon.Lambda.DurableExecution.Testing). Read DurableTestRunner.cs, TestResult.cs, TestStep.cs, OperationKind.cs, OperationStatus.cs. TestResult accessors: GetStep(name), FindStep, GetSteps, GetStepsByStatus, Steps, EnsureSucceeded(), IsSucceeded, Result, Error. TestStep: Kind, Status, Name, Attempt, GetResult<T>(), GetError(), GetCallbackId(). OperationKind {Step,Wait,Callback,ChainedInvoke,Context,Execution}. OperationStatus is a static string-const class {Started,Succeeded,Failed,Pending,TimedOut,Cancelled,Stopped,Ready}. filter-by-status uses GetStepsByStatus(OperationStatus.X). Use xunit [Fact]/Assert. Each test file is self-contained OR references a companion example class: if it references a workflow class, DEFINE that workflow inside the test file (do not invent WorkflowForTest — pass the workflow method or an inline async (input,ctx)=>... to the runner ctor).',
  },
  {
    slug: 'testing-authoring', md: 'docs/testing/authoring.md', kind: 'testing',
    bases: ['testing/authoring/minimal-test', 'testing/authoring/test-branching', 'testing/authoring/test-failure', 'testing/authoring/test-retries'],
    note: 'DurableTestRunner<TIn,TOut>(handler, new TestRunnerOptions { SkipTime = true }) then await runner.RunAsync(input). Read DurableTestRunner.cs + TestRunnerOptions.cs. minimal-test: construct runner, RunAsync, EnsureSucceeded/assert Result. test-failure: assert result.IsFailed / result.Error. test-retries: assert a step\'s Attempt via result.GetStep(name).Attempt. Make each file self-contained: define the workflow under test in the same file (inline lambda or a static method), pass it to the runner ctor. Use xunit. Do NOT invent WorkflowForTest.',
  },
  {
    slug: 'testing-cloud-runner', md: 'docs/testing/cloud-runner.md', kind: 'testing',
    bases: ['testing/cloud-runner/cloud-runner', 'testing/cloud-runner/cloud-runner-timeout'],
    note: 'Use CloudDurableTestRunner<TIn,TOut> + CloudTestRunnerOptions (read CloudDurableTestRunner.cs, CloudTestRunnerOptions.cs). CloudTestRunnerOptions: InitialPollInterval, PollInterval, DefaultTimeout, Serializer. Ctor and RunAsync/StartAsync/WaitForResultAsync mirror the local runner. cloud-runner-timeout: set DefaultTimeout or pass a timeout to RunAsync. Self-contained files; define the workflow inline. Use xunit.',
  },
  {
    slug: 'testing-workflow-patterns', md: 'docs/testing/workflow-patterns.md', kind: 'testing',
    bases: ['testing/examples/child-context', 'testing/examples/long-waits', 'testing/examples/parallel-workflow', 'testing/examples/partial-failures', 'testing/examples/polling', 'testing/examples/sequential-workflow'],
    note: 'End-to-end test examples using DurableTestRunner. long-waits: rely on TestRunnerOptions.SkipTime=true (default) so day-long WaitAsync completes instantly. parallel-workflow/partial-failures: ParallelAsync + IBatchResult<T> (inspect .Failed / ThrowIfError, and OperationKind.Context steps). polling: WaitForConditionAsync. Each file self-contained: define the workflow in the file, pass to runner ctor. Use xunit. Read DurableTestRunner.cs, TestResult.cs, TestStep.cs, and the operation config source as needed.',
  },
]

const AUTHOR_SCHEMA = {
  type: 'object', additionalProperties: false,
  required: ['page', 'compilableFiles', 'signatureFiles', 'tabGroupsTotal', 'tabGroupsWithCsharp', 'allCompiled', 'notes'],
  properties: {
    page: { type: 'string' },
    compilableFiles: { type: 'array', items: { type: 'string' } },
    signatureFiles: { type: 'array', items: { type: 'string' }, description: 'non-compilable fragment/pseudo-signature files' },
    tabGroupsTotal: { type: 'integer' },
    tabGroupsWithCsharp: { type: 'integer' },
    allCompiled: { type: 'boolean' },
    notes: { type: 'string' },
  },
}

const VERDICT_SCHEMA = {
  type: 'object', additionalProperties: false,
  required: ['page', 'compileClean', 'tabsComplete', 'signaturesMatchSource', 'issues', 'verdict'],
  properties: {
    page: { type: 'string' },
    compileClean: { type: 'boolean' },
    tabsComplete: { type: 'boolean' },
    signaturesMatchSource: { type: 'boolean' },
    issues: { type: 'array', items: { type: 'string' } },
    verdict: { type: 'string', enum: ['PASS', 'FAIL'] },
  },
}

function commonFor(kind) {
  const harness = kind === 'testing'
    ? `scripts/csharp-verify/dll-verify-testing.csproj (references the testing SDK + xunit)`
    : `scripts/csharp-verify/dll-verify.csproj (references the core SDK + AWSSDK.Lambda)`
  const srcNote = kind === 'testing'
    ? `Testing SDK source: ${TESTING_SRC} (DurableTestRunner.cs, CloudDurableTestRunner.cs, TestRunnerOptions.cs, CloudTestRunnerOptions.cs, TestResult.cs, TestStep.cs, OperationKind.cs, OperationStatus.cs). Core SDK source: ${SDK_SRC}.`
    : `Core SDK source: ${SDK_SRC} (IDurableContext.cs, StepConfig.cs, RetryStrategy.cs, and the config types).`
  return `
You are extending an AWS documentation site that shows every SDK example in content tabs
ordered TypeScript, Python, Java. Add a fourth "C#" tab (the .NET SDK) to every tab group
on ONE page, and create the backing C# example files the tabs include.

HARD RULES:
- Tab order is ALWAYS TypeScript, Python, Java, C#. Add C# LAST in each group, right after Java.
- Every "=== \\"Java\\"" block gets a matching "=== \\"C#\\"" block. Do not skip any group.
- Snippet includes use exactly:  --8<-- "examples/csharp/<base>.cs"  in a \`\`\`csharp fence,
  indented 4 spaces, mirroring the Java block. If the Java tab shows inline code (no --8<--),
  write the C# inline too (no file).
- The C# example must be FUNCTIONALLY EQUIVALENT to the TS/Python/Java trio for that base-path.
- VERIFY EVERY API DETAIL AGAINST SOURCE before writing. Do not guess a method name, parameter
  order, type, or return type. Recurse into referenced types.

GROUND TRUTH TO READ FIRST:
1. Exemplar committed pages that already have all 4 tabs: docs/sdk-reference/operations/step.md
   and the C# example files under examples/csharp/operations/steps/*.cs and
   examples/csharp/operations/child-contexts/*.cs (incl. test-child-context.cs for the testing pattern).
   Also docs/sdk-reference/languages/csharp/index.md for the handler/serializer/programming-model story.
2. ${srcNote}
3. The existing examples/{typescript,python,java}/<base>.{ts,py,java} for each base-path.

C# FILE CONVENTIONS (match the committed examples/csharp files):
- Standalone, self-contained. Non-test example: a public class with a Handler returning
  Task<DurableExecutionInvocationOutput> delegating to DurableFunction.WrapAsync<TIn,TOut>(Workflow, input, context),
  and a Workflow(TIn, IDurableContext ctx). Define any record types in the file.
- Step/branch/child bodies are async lambdas: async (_, ct) => ... . Use ctx.Logger, never Console.WriteLine.
- Test example (testing pages): xunit [Fact], construct the runner with the workflow method or an
  inline async (input,ctx)=>... . NEVER invent members like WorkflowForTest; pass the actual delegate.
  If the test needs a workflow class, define it in the same file.
- Each file compiles in isolation, so duplicate record/class names ACROSS files are fine.
- Pseudo-signature / fragment files (mirroring a Java/TS file that just shows a signature, an
  interface, or a bare statement list with no class) are NOT compilable: list them under
  signatureFiles. Name them to match the base (keep an existing -signature suffix).

SELF-COMPILE each compilable file before returning, from ${REPO}, using a UNIQUE .verify dir per file:
  dotnet build ${harness} -p:ExampleFile="${REPO}/examples/csharp/<base>.cs" -p:BaseIntermediateOutputPath=".verify/<slug>-<n>/obj/" -p:BaseOutputPath=".verify/<slug>-<n>/bin/" -v q --nologo
Exit 0 = pass. Fix and recompile until green. Do NOT set allCompiled=true unless every compilable file exits 0.
Note: .verify dir paths must use forward slashes.`
}

phase('Author')

const results = await pipeline(
  PAGES,
  (p) => agent(
    `${commonFor(p.kind)}

YOUR PAGE: ${p.md}   (kind: ${p.kind})
SLUG (for .verify dirs): ${p.slug}
C# BASE-PATHS TO CREATE (mirror the existing TS/Py/Java files; write examples/csharp/<base>.cs):
${p.bases.map((b) => '  - ' + b).join('\n')}
PAGE NOTE: ${p.note}

Steps: read the exemplar + source + existing examples; write the .cs files (or inline C# for
inline groups); edit ${p.md} to add a C# tab after every Java block; compile every compilable
file until green; return the manifest. tabGroupsWithCsharp must equal tabGroupsTotal.`,
    { label: `author:${p.slug}`, phase: 'Author', schema: AUTHOR_SCHEMA, effort: 'high' },
  ),
  (author, p) => author == null ? null : agent(
    `Independently and adversarially VERIFY the C# tab work on ONE page. Assume the author erred.

PAGE: ${p.md}   (kind: ${p.kind})
AUTHOR MANIFEST: ${JSON.stringify(author)}
HARNESS: ${p.kind === 'testing' ? 'scripts/csharp-verify/dll-verify-testing.csproj' : 'scripts/csharp-verify/dll-verify.csproj'}
SOURCE: core=${SDK_SRC}${p.kind === 'testing' ? ', testing=' + TESTING_SRC : ''}

Checks:
1. COMPILE every file in compilableFiles independently, fresh unique .verify dirs (forward slashes),
   from ${REPO}:
     dotnet build <HARNESS> -p:ExampleFile="${REPO}/<file>" -p:BaseIntermediateOutputPath=".verify/verify-${p.slug}-<n>/obj/" -p:BaseOutputPath=".verify/verify-${p.slug}-<n>/bin/" -v q --nologo
   compileClean = all exit 0. Report any failing file with its error. If a test file references a
   companion workflow class in another file, compile them together (both in one temp project) and say so.
2. TABS: read ${p.md}. Every content-tab group must end with exactly 4 tabs in order
   TypeScript, Python, Java, C#. tabsComplete reflects this; report any wrong group (quote nearest heading).
3. SOURCE FIDELITY: open the relevant source and confirm each C# snippet's method names, parameter
   order/names/types, generic args, and return types match EXACTLY. For testing pages, confirm the
   runner/TestResult/TestStep/enum members actually exist (no invented members like WorkflowForTest).
   signaturesMatchSource reflects this; report each CONFIRMED mismatch.
4. Do NOT compile signatureFiles.

verdict = PASS only if compileClean && tabsComplete && signaturesMatchSource.`,
    { label: `verify:${p.slug}`, phase: 'Verify', schema: VERDICT_SCHEMA, effort: 'high' },
  ),
)

const clean = results.filter(Boolean)
const passed = clean.filter((r) => r.verdict === 'PASS')
const failed = clean.filter((r) => r.verdict === 'FAIL')
log(`Verified ${clean.length}/${PAGES.length}: ${passed.length} PASS, ${failed.length} FAIL`)

return {
  summary: `${passed.length}/${PAGES.length} pages passed`,
  passed: passed.map((r) => r.page),
  failed: failed.map((r) => ({ page: r.page, issues: r.issues })),
  missing: PAGES.filter((p) => !clean.some((r) => r.page && r.page.includes(p.slug))).map((p) => p.md),
}

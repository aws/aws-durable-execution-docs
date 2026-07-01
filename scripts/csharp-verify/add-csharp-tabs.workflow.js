export const meta = {
  name: 'add-csharp-tabs',
  description: 'Add a verified C# (.NET) tab to every SDK-reference operation and concept page',
  phases: [
    { title: 'Author', detail: 'one agent per page: write C# examples + add === "C#" tabs, self-compile' },
    { title: 'Verify', detail: 'independent agent per page: recompile + check each .cs against IDurableContext.cs' },
  ],
}

// ---------------------------------------------------------------------------
// Ground truth (all verified earlier against source):
//   SDK source dir : C:/dev/repos/aws-lambda-dotnet/Libraries/src/Amazon.Lambda.DurableExecution
//   SDK docs/core  : <SDK>/docs/core/{steps,wait,wait-for-condition,callbacks,child-contexts,parallel,cancellation}.md
//   Exemplar page  : docs/sdk-reference/operations/step.md  (already has all 4 tabs)
//   Exemplar C#    : examples/csharp/operations/steps/*.cs  (already compile-verified)
//   Harness        : scripts/csharp-verify/dll-verify.csproj  (references prebuilt SDK DLLs, parallel-safe)
// ---------------------------------------------------------------------------

const SDK_SRC = 'C:/dev/repos/aws-lambda-dotnet/Libraries/src/Amazon.Lambda.DurableExecution'

// Per page: the markdown file, the examples/<lang>/ base-paths (lang-stripped, no
// extension) that already exist for TS/Py/Java and that C# must mirror 1:1, plus any
// SDK source files especially relevant to that page and a divergence note.
const PAGES = [
  {
    slug: 'wait',
    md: 'docs/sdk-reference/operations/wait.md',
    bases: ['core/wait/basic-wait','core/wait/wait-signature','core/wait/duration-signature','core/wait/duration-helpers','core/wait/named-wait','core/wait/async-wait','core/wait/test-multiple-waits'],
    source: ['IDurableContext.cs (WaitAsync)'],
    note: 'WaitAsync(TimeSpan duration, string? name = null, CancellationToken = default). Minimum 1 second. .NET has no separate "duration helpers" type; use TimeSpan.FromMinutes/Hours/etc. The duration-signature / duration-helpers files should show TimeSpan usage.',
  },
  {
    slug: 'wait-for-condition',
    md: 'docs/sdk-reference/operations/wait-for-condition.md',
    bases: ['core/wait/wait-for-condition','core/wait/wait-for-condition-signature','core/wait/wait-strategy-signature','core/wait/custom-strategy','core/wait/strategy-helper'],
    source: ['IDurableContext.cs (WaitForConditionAsync)','WaitForConditionConfig.cs','IWaitStrategy.cs','WaitStrategy.cs','IConditionCheckContext.cs','WaitForConditionException.cs','docs/core/wait-for-condition.md'],
    note: 'WaitForConditionAsync<TState>(Func<TState,IConditionCheckContext,CancellationToken,Task<TState>> check, WaitForConditionConfig<TState> config, string? name = null, CancellationToken = default). config is REQUIRED and generic on TState (carries InitialState + IWaitStrategy<TState>).',
  },
  {
    slug: 'callback',
    md: 'docs/sdk-reference/operations/callback.md',
    bases: ['operations/callbacks/basic-example','operations/callbacks/wait-for-callback-example','operations/callbacks/create-callback-signature','operations/callbacks/wait-for-callback-signature','operations/callbacks/callback-config'],
    source: ['IDurableContext.cs (CreateCallbackAsync, WaitForCallbackAsync)','ICallback.cs','CallbackConfig.cs','WaitForCallbackConfig.cs','IWaitForCallbackContext.cs','CallbackException.cs','docs/core/callbacks.md'],
    note: 'CreateCallbackAsync<T>(string? name, CallbackConfig? config, CancellationToken) returns Task<ICallback<T>>; ICallback<T> exposes CallbackId and GetResultAsync(ct). WaitForCallbackAsync<T>(Func<string,IWaitForCallbackContext,CancellationToken,Task> submitter, string? name, WaitForCallbackConfig? config, CancellationToken).',
  },
  {
    slug: 'invoke',
    md: 'docs/sdk-reference/operations/invoke.md',
    bases: ['operations/invoke/process-order','operations/invoke/invoke-method-signature','operations/invoke/invoke-with-config','operations/invoke/handle-invocation-error'],
    source: ['IDurableContext.cs (InvokeAsync)','InvokeConfig.cs','InvokeException.cs'],
    note: 'InvokeAsync<TPayload,TResult>(string functionName, TPayload payload, string? name, InvokeConfig? config, CancellationToken). functionName must be qualified (version/alias/$LATEST).',
  },
  {
    slug: 'parallel',
    md: 'docs/sdk-reference/operations/parallel.md',
    bases: ['operations/parallel/simple-parallel','operations/parallel/named-branches','operations/parallel/pass-arguments','operations/parallel/parallel-config','operations/parallel/completion-config','operations/parallel/error-handling','operations/parallel/nested-parallel'],
    source: ['IDurableContext.cs (ParallelAsync x2)','ParallelConfig.cs','DurableBranch.cs','IBatchResult.cs','IBatchItem.cs','CompletionConfig.cs','CompletionReason.cs','docs/core/parallel.md'],
    note: 'Two overloads: ParallelAsync<T>(IReadOnlyList<Func<IDurableContext,CancellationToken,Task<T>>> branches, ...) and ParallelAsync<T>(IReadOnlyList<DurableBranch<T>> branches, ...). Returns Task<IBatchResult<T>>. Use IBatchResult.ThrowIfError for strict success.',
  },
  {
    slug: 'map',
    md: 'docs/sdk-reference/operations/map.md',
    bases: ['operations/map/simple-map','operations/map/map-signature','operations/map/map-function','operations/map/named-map','operations/map/map-config','operations/map/completion-config','operations/map/error-handling','operations/map/nested-map'],
    source: ['IDurableContext.cs (MapAsync)','MapConfig.cs','IBatchResult.cs','IBatchItem.cs','CompletionConfig.cs','CompletionReason.cs'],
    note: 'MapAsync<TItem,TResult>(IReadOnlyList<TItem> items, Func<IDurableContext,TItem,int,IReadOnlyList<TItem>,CancellationToken,Task<TResult>> func, string? name, MapConfig? config, CancellationToken). The func receives (ctx, item, index, allItems, ct). There is no docs/core/map.md — rely on source + XML docs.',
  },
  {
    slug: 'child-context',
    md: 'docs/sdk-reference/operations/child-context.md',
    bases: ['operations/child-contexts/basic-child-context','operations/child-contexts/run-in-child-context-signature','operations/child-contexts/child-config-signature','operations/child-contexts/context-function','operations/child-contexts/pass-arguments','operations/child-contexts/named-child-context','operations/child-contexts/concurrent-child-contexts','operations/child-contexts/test-child-context'],
    source: ['IDurableContext.cs (RunInChildContextAsync x2)','ChildContextConfig.cs','DurableExecutionException.cs (ChildContextException)','docs/core/child-contexts.md'],
    note: 'RunInChildContextAsync<T>(Func<IDurableContext,CancellationToken,Task<T>> func, string? name, ChildContextConfig? config, CancellationToken) and a void overload. On failure surfaces ChildContextException; ChildContextConfig.ErrorMapping remaps it.',
  },
  {
    slug: 'errors',
    md: 'docs/sdk-reference/error-handling/errors.md',
    bases: ['sdk-reference/error-handling/basic-error-handling','sdk-reference/error-handling/exception-hierarchy','sdk-reference/error-handling/validation-error','sdk-reference/error-handling/step-interrupted','sdk-reference/error-handling/serdes-error'],
    source: ['DurableExecutionException.cs','CallbackException.cs','InvokeException.cs','WaitForConditionException.cs','ErrorObject.cs'],
    note: 'The exception-hierarchy file is typically a pseudo/tree listing (non-compilable) — mirror it as such. Map each language exception to its .NET equivalent by reading the *Exception.cs files. .NET uses OperationCanceledException for token-driven cancellation (not a step failure).',
  },
  {
    slug: 'retries',
    md: 'docs/sdk-reference/error-handling/retries.md',
    bases: ['sdk-reference/error-handling/exponential-backoff','sdk-reference/error-handling/retry-strategy-config-signature','sdk-reference/error-handling/jitter-strategy-signature','sdk-reference/error-handling/linear-retry-strategy','sdk-reference/error-handling/linear-retry-strategy-config-signature','sdk-reference/error-handling/retry-strategy-signature','sdk-reference/error-handling/custom-retry-strategy','sdk-reference/error-handling/retry-presets','sdk-reference/error-handling/with-retry-helper','sdk-reference/error-handling/retry-specific-errors'],
    source: ['RetryStrategy.cs','IRetryStrategy.cs','StepConfig.cs','docs/core/steps.md (retry section)'],
    note: 'HIGH DIVERGENCE. .NET retry API differs from the others: static factory RetryStrategy.{Default,Transient,None,Exponential(...),FromDelegate(...)} returning IRetryStrategy; RetryDecision.{DoNotRetry(),RetryAfter(delay)}; enum JitterStrategy {None,Full,Half}. Exponential(maxAttempts, initialDelay, maxDelay, backoffRate, jitter, retryableExceptions, retryableMessagePatterns). There is NO built-in "linear" strategy — for the linear-* bases, show RetryStrategy.FromDelegate implementing a constant/linear delay. "presets" map to Default/Transient/None. "with-retry helper" maps to setting StepConfig.RetryStrategy. "retry-specific-errors" maps to the retryableExceptions param. The C# prose in these tabs will legitimately differ from other languages.',
  },
  {
    slug: 'serialization',
    md: 'docs/sdk-reference/state/serialization.md',
    bases: ['sdk-reference/serialization/Walkthrough','sdk-reference/serialization/SerdesInterface','sdk-reference/serialization/OrderSerDes','sdk-reference/serialization/StepConfigExample','sdk-reference/serialization/CallbackConfigExample','sdk-reference/serialization/MapConfigExample','sdk-reference/serialization/PassThroughSerdesExample'],
    source: ['DurableFunction.cs (ILambdaSerializer story)','StepConfig.cs','CallbackConfig.cs','MapConfig.cs','README.md (AOT section)'],
    note: 'HIGH DIVERGENCE. .NET has NO per-operation serdes: one ILambdaSerializer is registered on ILambdaContext.Serializer and used for everything. StepConfig/CallbackConfig/MapConfig do NOT expose a serdes property (verify in source). The C# tabs must explain the single-serializer model: DefaultLambdaJsonSerializer for reflection, SourceGeneratorLambdaJsonSerializer<TContext> for AOT/trim. SerdesInterface/OrderSerDes/PassThrough bases: show how to register a custom ILambdaSerializer at the host boundary instead of a per-step SerDes. Keep C# prose accurate even though it diverges structurally.',
  },
  {
    slug: 'logging',
    md: 'docs/sdk-reference/observability/logging.md',
    bases: ['sdk-reference/observability/basic-usage','sdk-reference/observability/step-context-logger','sdk-reference/observability/replay-suppression','sdk-reference/observability/powertools-logger'],
    source: ['IDurableContext.cs (Logger, ConfigureLogger, IStepContext.Logger)','LoggerConfig.cs'],
    note: 'ctx.Logger is a Microsoft.Extensions.Logging.ILogger, replay-safe by default. ConfigureLogger(new LoggerConfig { CustomLogger = ..., ModeAware = false }) swaps logger / disables replay suppression. IStepContext.Logger is the step-scoped logger. Powertools: register via ConfigureLogger CustomLogger.',
  },
  {
    slug: 'custom-lambda-client',
    md: 'docs/sdk-reference/configuration/custom-lambda-client.md',
    bases: ['configuration/custom-client'],
    source: ['DurableFunction.cs (WrapAsync overloads taking IAmazonLambda lambdaClient)'],
    note: 'The custom client is passed as the 4th arg to DurableFunction.WrapAsync<TIn,TOut>(workflow, input, context, lambdaClient) where lambdaClient is an IAmazonLambda. No DurableConfig object like Java; the client is a WrapAsync parameter.',
  },
]

const AUTHOR_SCHEMA = {
  type: 'object',
  additionalProperties: false,
  required: ['page', 'compilableFiles', 'signatureFiles', 'tabGroupsTotal', 'tabGroupsWithCsharp', 'allCompiled', 'notes'],
  properties: {
    page: { type: 'string' },
    compilableFiles: { type: 'array', items: { type: 'string' }, description: 'examples/csharp/... paths written that must compile' },
    signatureFiles: { type: 'array', items: { type: 'string' }, description: 'pseudo-signature / non-compilable example paths written' },
    tabGroupsTotal: { type: 'integer', description: 'number of content-tab groups on the page' },
    tabGroupsWithCsharp: { type: 'integer', description: 'how many now have a C# tab (should equal tabGroupsTotal)' },
    allCompiled: { type: 'boolean', description: 'true only if every compilableFile built green via the harness' },
    notes: { type: 'string', description: 'divergences, anything the reviewer should scrutinize' },
  },
}

const VERDICT_SCHEMA = {
  type: 'object',
  additionalProperties: false,
  required: ['page', 'compileClean', 'tabsComplete', 'signaturesMatchSource', 'issues', 'verdict'],
  properties: {
    page: { type: 'string' },
    compileClean: { type: 'boolean', description: 'every compilable C# file for this page built green in an independent run' },
    tabsComplete: { type: 'boolean', description: 'every tab group ends with exactly 4 tabs in order TS, Python, Java, C#' },
    signaturesMatchSource: { type: 'boolean', description: 'every C# API usage matches IDurableContext.cs / config source exactly (param order, names, types, return types)' },
    issues: { type: 'array', items: { type: 'string' }, description: 'CONFIRMED problems, most severe first; empty if clean' },
    verdict: { type: 'string', enum: ['PASS', 'FAIL'] },
  },
}

const COMMON = `
You are extending an AWS documentation site that shows every SDK example in content tabs
ordered TypeScript, Python, Java. Your job for ONE page is to add a fourth "C#" tab
(the .NET SDK, Amazon.Lambda.DurableExecution) to every tab group, and to create the
backing C# example files the tabs include.

HARD RULES (from the repo's authoring guide):
- Tab order is ALWAYS TypeScript, then Python, then Java, then C#. Add C# LAST in each group.
- Every "=== \\"Java\\"" block in the page must gain a matching "=== \\"C#\\"" block right after it
  (after the Java code fence / prose). Do not skip any group.
- The C# example must be FUNCTIONALLY EQUIVALENT to the existing TS/Python/Java trio for the
  same base-path: same scenario, same values, idiomatic .NET.
- Snippet includes use exactly:  --8<-- "examples/csharp/<base>.cs"  inside a  \`\`\`csharp  fence,
  indented 4 spaces under the "=== \\"C#\\"" line, mirroring the Java block's structure.
- Some existing examples are inline code (no --8<-- include) or pseudo-signatures. Match whatever
  the Java tab does for that group: if Java uses --8<--, you use --8<-- and create the file; if Java
  shows an inline signature/interface, write the C# equivalent inline in the tab (no file).
- VERIFY EVERY API DETAIL AGAINST SOURCE. Read the SDK source files before writing. Do not guess a
  method name, parameter order, type, or return type. Recurse into referenced config types.

GROUND TRUTH TO READ FIRST (in this order):
1. The exemplar page docs/sdk-reference/operations/step.md — it already has all 4 tabs; copy its
   tab structure, indentation, and the shape of its C# prose exactly.
2. The exemplar C# files examples/csharp/operations/steps/*.cs — copy their file style (usings,
   handler shape via DurableFunction.WrapAsync, record types, async lambdas (_, ct) => ...).
3. The SDK source in ${SDK_SRC} — read IDurableContext.cs plus the page-specific files listed below.
4. The existing examples/{typescript,python,java}/<base>.{ts,py,java} for each base-path, to match scenario/values.

C# EXAMPLE FILE CONVENTIONS (match the existing csharp/operations/steps files):
- Standalone, self-contained: usings, a public class with a Handler method that returns
  Task<DurableExecutionInvocationOutput> and delegates to DurableFunction.WrapAsync<TIn,TOut>(Workflow, input, context),
  and a private Workflow(TIn input, IDurableContext ctx) method. Define any record types the file needs
  IN THE FILE (each file compiles in isolation, so duplicate record names ACROSS files are fine).
- Step/branch/child bodies are async lambdas: async (ctx, ct) => ... or async (_, ct) => ...
- Use ctx.Logger, never Console.WriteLine.
- Pseudo-signature files (mirroring a Java/TS file that just shows a method signature or interface)
  are NOT compilable: give them a name ending in -signature.cs when the base already ends in -signature,
  and list them under signatureFiles so they are skipped by the compiler. If a base does NOT end in
  -signature but its Java/TS counterpart is still non-compilable (e.g. an interface listing or an
  exception-hierarchy tree), also treat it as a signature file and note that.

SELF-COMPILE EACH COMPILABLE FILE before returning. From the repo root
C:/dev/repos/aws-durable-execution-docs run, for each compilable file:
  dotnet build scripts/csharp-verify/dll-verify.csproj -p:ExampleFile="C:/dev/repos/aws-durable-execution-docs/examples/csharp/<base>.cs" -p:BaseIntermediateOutputPath=".verify/<slug>-<n>/obj/" -p:BaseOutputPath=".verify/<slug>-<n>/bin/" -v q --nologo
Use a UNIQUE .verify/<slug>-<n>/ dir per file (never reuse a dir across concurrent builds).
Exit code 0 = pass. If it fails, read the error, fix the .cs (or the SDK usage), recompile until green.
Do NOT mark allCompiled=true unless every compilable file exits 0.
`

phase('Author')

const results = await pipeline(
  PAGES,
  (p) => agent(
    `${COMMON}

YOUR PAGE: ${p.md}
SLUG (for .verify dirs): ${p.slug}
C# BASE-PATHS TO CREATE (mirror the existing TS/Py/Java files for each; write examples/csharp/<base>.cs):
${p.bases.map((b) => '  - ' + b).join('\n')}
PAGE-SPECIFIC SOURCE TO READ: ${p.source.join(', ')}
DIVERGENCE NOTE: ${p.note}

Steps:
1. Read the exemplar, the SDK source files listed, and the existing examples for each base-path.
2. Write examples/csharp/<base>.cs for every base whose Java counterpart uses --8<--.
   For inline/pseudo-signature groups, prepare the inline C# instead.
3. Edit ${p.md}: after every "=== \\"Java\\"" block, insert the "=== \\"C#\\"" block (include or inline to match).
4. Compile every compilable file via the harness until all are green.
5. Return the manifest. Set tabGroupsWithCsharp = the number of groups you added C# to; it must equal tabGroupsTotal.`,
    { label: `author:${p.slug}`, phase: 'Author', schema: AUTHOR_SCHEMA, effort: 'high' },
  ),
  (author, p) => author == null ? null : agent(
    `Independently VERIFY the C# tab work on ONE documentation page. Be adversarial: assume the author
made mistakes. Confirm each issue before reporting it.

PAGE: ${p.md}
AUTHOR MANIFEST: ${JSON.stringify(author)}
SDK SOURCE DIR: ${SDK_SRC}

Checks:
1. COMPILE: independently run the harness on EVERY file the author listed in compilableFiles, using
   fresh unique .verify dirs (e.g. .verify/verify-${p.slug}-<n>/). From C:/dev/repos/aws-durable-execution-docs:
     dotnet build scripts/csharp-verify/dll-verify.csproj -p:ExampleFile="C:/dev/repos/aws-durable-execution-docs/<file>" -p:BaseIntermediateOutputPath=".verify/verify-${p.slug}-<n>/obj/" -p:BaseOutputPath=".verify/verify-${p.slug}-<n>/bin/" -v q --nologo
   compileClean = true only if all exit 0. Report any file that fails with its error.
2. TABS: read ${p.md}. Every content-tab group must end with exactly 4 tabs in the order
   TypeScript, Python, Java, C#. No group left with only 3. No duplicate/misordered tabs.
   tabsComplete reflects this. Report any group that is wrong (quote the nearest heading).
3. SOURCE FIDELITY: for each C# file/inline snippet, open the relevant source in the SDK dir and confirm
   the method name, parameter order, parameter names, types, generic args, and return type match EXACTLY
   (IDurableContext.cs and the page's config types). Report any mismatch as a CONFIRMED issue.
   signaturesMatchSource reflects this.
4. Do NOT try to compile signatureFiles.

Return the verdict. verdict = PASS only if compileClean && tabsComplete && signaturesMatchSource.`,
    { label: `verify:${p.slug}`, phase: 'Verify', schema: VERDICT_SCHEMA, effort: 'high' },
  ),
)

const clean = results.filter(Boolean)
const passed = clean.filter((r) => r.verdict === 'PASS')
const failed = clean.filter((r) => r.verdict === 'FAIL')

log(`Verified ${clean.length}/${PAGES.length} pages: ${passed.length} PASS, ${failed.length} FAIL`)

return {
  summary: `${passed.length}/${PAGES.length} pages passed`,
  passed: passed.map((r) => r.page),
  failed: failed.map((r) => ({ page: r.page, issues: r.issues })),
  missing: PAGES.filter((p) => !clean.some((r) => r.page && r.page.includes(p.slug))).map((p) => p.md),
}

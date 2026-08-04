# Choosing a durable workflow engine

Long-running application logic (order sagas, human approvals, provisioning
flows, polling loops) tends to break when a process crashes, a deployment ships
mid-flight, or an external call hangs. "Durable execution" engines solve this:
you write mostly ordinary code, and the platform checkpoints progress so the
work resumes exactly where it stopped after any failure.

This article compares the major options and, at the end, offers a practical way
to choose. The comparisons are meant to be fair. Every engine here is a
reasonable choice for the right workload.

## What "durable execution" means

A durable engine records the outcome of each completed unit of work to a
journal. When the program resumes, it replays your code and returns the stored
result for anything already done, rather than re-running it. Two consequences
follow for every engine in this space:

- The orchestration code must be deterministic. Side effects (API calls, DB
    writes) go in a recorded unit, not in the replayed control flow.
- Waiting is cheap. A workflow can sleep for days without holding compute,
    because its state lives in the journal, not in memory.

The engines differ mainly in **where they run**, **how you write the code**,
and **who operates the infrastructure**.

## The landscape

### Code-first durable execution

These let you write workflows as normal code with loops, conditionals, and
error handling.

- **Temporal** is the category leader. You write workflows and activities in
    Go, Java, TypeScript, Python, or .NET. It runs as a separate cluster (self
    hosted) or as Temporal Cloud, and you always operate your own worker
    processes. Deep feature set: signals, queries, child workflows, a rich web
    UI, and long history. Portable across clouds and on-prem.
- **Cadence** is Temporal's predecessor (from Uber), similar model, smaller
    ecosystem today.
- **Restate** is a newer, lightweight engine with a single-binary server and
    low-latency execution. SDKs are open source; the server is source-available.
- **DBOS** is an embedded library: workflow state lives in Postgres and runs
    inside your application, so there is no separate orchestrator to operate.
- **Hatchet** is a Postgres-backed task and workflow engine, self-hostable.
- **Inngest** and **Trigger.dev** target application developers with
    event-driven durable functions and strong TypeScript ergonomics. Both offer
    open-source SDKs plus a managed cloud.
- **AWS Lambda Durable Functions** brings durable execution to Lambda itself.
    You write a handler that calls `step()`, `wait()`, `waitForCondition()`, and
    `callback()`; the Lambda service checkpoints progress. SDKs for TypeScript,
    Python, Java, and C#. There is no cluster and no worker fleet to run.
- **Azure Durable Functions** is the closest analog to Lambda Durable
    Functions: durable orchestration native to Azure Functions, with a managed
    storage backend and a mature feature set (including durable entities).

### State-machine and DAG orchestrators

These describe the workflow as a graph or state machine rather than as
imperative code.

- **AWS Step Functions** models workflows as JSON/ASL state machines. Strong
    AWS-service integrations; not a code-first authoring model.
- **Netflix Conductor / Orkes** orchestrates microservices via JSON-defined
    workflows.
- **Apache Airflow** (and **Prefect**, **Dagster**) schedule DAGs of tasks.
    These are batch data-pipeline schedulers (ETL/ELT, ML pipelines) with
    first-class cron, backfills, and data intervals. They solve a different
    problem than per-event application durability.

### Platform-tied options

- **Cloudflare Workflows** run on Cloudflare Workers.
- **Vercel Workflow** runs on the Vercel platform.
- **Upstash Workflow** pairs an open-source SDK with the managed QStash backend.

## How they differ

| Dimension | Temporal | Azure Durable Functions | AWS Lambda Durable Functions | Step Functions | Airflow |
|---|---|---|---|---|---|
| Programming model | Code-first (workflow + activity) | Code-first (orchestrator + activity) | Code-first (handler + inline steps) | JSON state machine | Python DAG |
| Infrastructure you run | Workers always; cluster unless Cloud | Managed by Azure Functions | None (runs on Lambda) | None (managed) | Scheduler + workers + DB (or managed) |
| Portability | Any cloud / on-prem | Azure | AWS | AWS | Any (self-host or managed) |
| Long idle waits | Cheap | Cheap | Cheap (no compute billed) | Cheap | Weak (historically) |
| Scaling | Scale worker fleet | Functions host plans | Lambda auto-scale | Managed | Scale workers |
| Maturity | Very high | Very high | New | High | Very high (for data) |

Two honest points:

- **Temporal** is the most capable and portable engine, and the right default
    if you need multi-cloud, warm-worker latency, or deep operational
    introspection. The cost is that you operate workers (and, unless you buy
    Temporal Cloud, a cluster and database).
- **Airflow and friends** are excellent for scheduled batch data pipelines and
    a poor fit for per-event application workflows. Don't cross-shop them for the
    wrong job.

## How to choose

Ask three questions.

1. **Do you need to run off AWS, or across clouds?** If yes, a portable engine
    like Temporal is the natural fit.
2. **Is this scheduled batch data processing?** If yes, reach for Airflow,
    Prefect, or Dagster.
3. **Is this event-driven application logic on AWS, and do you want durability
    without operating orchestration infrastructure?** If yes, AWS Lambda Durable
    Functions is worth a serious look.

That third case is common, and it is where Lambda Durable Functions fits
particularly well. You get durable execution as a property of Lambda itself:
no cluster, no worker fleet, scale-to-zero, and long waits (human approval,
external callbacks, polling) that do not bill compute while suspended. If your
workload already lives on Lambda and your main reason for reaching past it was
"I need this to survive crashes and long waits," Lambda Durable Functions often
removes the reason to add a separate system at all.

None of this makes the other engines wrong. Temporal's portability and
introspection, Azure Durable Functions' maturity on Azure, and Airflow's
scheduling strengths are real. The point is narrower: for AWS-native,
event-driven, durability-first workloads, starting with Lambda Durable Functions
lets you defer the operational weight of a standalone orchestrator until you have
a concrete reason to take it on.

## Further reading

- AWS Lambda Durable Functions documentation: <https://docs.aws.amazon.com/durable-execution/>
- Temporal documentation: <https://docs.temporal.io/>
- Azure Durable Functions documentation: <https://learn.microsoft.com/azure/azure-functions/durable/>

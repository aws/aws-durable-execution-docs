use aws_durable_execution_sdk as durable;
use serde::Serialize;

#[derive(Serialize)]
struct ResultPair {
    a: String,
    b: String,
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<ResultPair, durable::BoxError> {
    let a = ctx
        .run_in_child_context(
            |child| async move { Ok(child.step(|_| async { Ok(fetch_a()) }).await?) },
        )
        .spawn();
    let b = ctx
        .run_in_child_context(
            |child| async move { Ok(child.step(|_| async { Ok(fetch_b()) }).await?) },
        )
        .spawn();
    Ok(ResultPair {
        a: a.await?,
        b: b.await?,
    })
}

fn fetch_a() -> String {
    "result-a".to_owned()
}

fn fetch_b() -> String {
    "result-b".to_owned()
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}

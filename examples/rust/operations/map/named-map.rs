use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct UsersEvent {
    user_ids: Vec<String>,
}

async fn handler(
    event: UsersEvent,
    ctx: durable::DurableContext,
) -> Result<Vec<String>, durable::BoxError> {
    let results = ctx
        .map(event.user_ids, |ctx, user_id, index| async move {
            Ok(ctx
                .step(move |_| async move { Ok(format!("processed-{user_id}")) })
                .name(format!("process-{index}"))
                .await?)
        })
        .name("process-users")
        .await?;

    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}

use aws_durable_execution_sdk as durable;
use durable::StepSemantics;
use durable::builders::RetryStrategyConfig;
use serde::{Deserialize, Serialize};

#[derive(Clone, Serialize, Deserialize)]
struct User {
    id: String,
    name: String,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct SignupEvent {
    user: User,
    amount: f64,
    card_token: String,
}

fn user_store_upsert(user: &User) -> User {
    user.clone()
}

fn payment_service_charge(_amount: f64, card_token: &str) -> String {
    format!("rcpt-{card_token}")
}

async fn handler(
    event: SignupEvent,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    let SignupEvent {
        user,
        amount,
        card_token,
    } = event;

    // At-least-once (default) for a retryable idempotent write.
    ctx.step(move |_| async move { Ok(user_store_upsert(&user)) })
        .name("upsert-user")
        .await?;

    // At-most-once for a side-effecting call.
    ctx.step(move |_| async move { Ok(payment_service_charge(amount, &card_token)) })
        .name("charge-payment")
        .semantics(StepSemantics::AtMostOncePerRetry)
        .retry_strategy_config(RetryStrategyConfig::no_retry())
        .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}

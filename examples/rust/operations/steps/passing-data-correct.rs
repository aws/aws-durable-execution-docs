use std::time::Duration;

use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Deserialize)]
struct SignupEvent {
    email: String,
}

fn register_user(email: &str) -> String {
    format!("user-{email}")
}

fn send_follow_up_email(_user_id: &str) {}

// ✅ CORRECT: user_id is returned from the step and restored from checkpoint on replay
async fn handler(
    event: SignupEvent,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    let user_id: String = ctx
        .step(move |_| async move { Ok(register_user(&event.email)) })
        .name("register-user")
        .await?;

    ctx.wait(Duration::from_mins(10))
        .name("follow-up-delay")
        .await?;

    ctx.step(move |_| async move {
        send_follow_up_email(&user_id);
        Ok(())
    })
    .name("send-follow-up-email")
    .await?;

    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}

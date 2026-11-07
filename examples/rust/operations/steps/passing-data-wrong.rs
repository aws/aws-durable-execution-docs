use std::sync::{Arc, Mutex};
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

// ❌ WRONG: the user_id mutation is lost on replay after the wait
async fn handler(
    event: SignupEvent,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    let user_id = Arc::new(Mutex::new(String::new()));

    let shared = Arc::clone(&user_id);
    ctx.step(move |_| async move {
        let registered = register_user(&event.email);
        *shared.lock().map_err(|e| e.to_string())? = registered;
        Ok(())
    })
    .name("register-user")
    .await?;

    ctx.wait(Duration::from_mins(10))
        .name("follow-up-delay")
        .await?;

    let shared = Arc::clone(&user_id);
    ctx.step(move |_| async move {
        let user_id = shared.lock().map_err(|e| e.to_string())?.clone();
        send_follow_up_email(&user_id); // user_id is "" on replay
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

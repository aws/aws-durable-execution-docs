use aws_durable_execution_sdk as durable;
use durable::StepSemantics;
use durable::builders::RetryStrategyConfig;
use serde::{Deserialize, Serialize};

#[derive(Clone, Deserialize)]
struct Order {
    id: String,
    total: f64,
}

#[derive(Deserialize)]
struct OrderEvent {
    order: Order,
    id: String,
}

#[derive(Serialize, Deserialize)]
struct Receipt {
    id: String,
    amount: f64,
}

#[derive(Serialize, Deserialize)]
struct User {
    id: String,
    name: String,
}

struct StepConfig {
    semantics: StepSemantics,
    retry_strategy: RetryStrategyConfig,
}

fn charge_payment(order: &Order) -> Receipt {
    Receipt {
        id: format!("rcpt-{}", order.id),
        amount: order.total,
    }
}

fn refund_payment(order: &Order) -> Receipt {
    Receipt {
        id: format!("rfnd-{}", order.id),
        amount: -order.total,
    }
}

fn user_store_get(id: &str) -> User {
    User {
        id: id.to_owned(),
        name: format!("user-{id}"),
    }
}

async fn handler(event: OrderEvent, ctx: durable::DurableContext) -> Result<(), durable::BoxError> {
    let payment_step_config = StepConfig {
        semantics: StepSemantics::AtMostOncePerRetry,
        retry_strategy: RetryStrategyConfig::no_retry(),
    };

    let idempotent_step_config = StepConfig {
        semantics: StepSemantics::default(),
        retry_strategy: RetryStrategyConfig::default(),
    };

    let OrderEvent { order, id } = event;
    let to_charge = order.clone();
    ctx.step(move |_| async move { Ok(charge_payment(&to_charge)) })
        .name("charge")
        .semantics(payment_step_config.semantics)
        .retry_strategy_config(payment_step_config.retry_strategy.clone())
        .await?;
    ctx.step(move |_| async move { Ok(refund_payment(&order)) })
        .name("refund")
        .semantics(payment_step_config.semantics)
        .retry_strategy_config(payment_step_config.retry_strategy)
        .await?;
    ctx.step(move |_| async move { Ok(user_store_get(&id)) })
        .name("fetch-user")
        .semantics(idempotent_step_config.semantics)
        .retry_strategy_config(idempotent_step_config.retry_strategy)
        .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}

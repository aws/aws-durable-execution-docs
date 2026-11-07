use std::time::Duration;

use aws_durable_execution_sdk as durable;

async fn wait_durations(ctx: &durable::DurableContext) -> Result<(), durable::BoxError> {
    ctx.wait(Duration::from_secs(30)).await?;

    ctx.wait(Duration::from_mins(5)).await?;

    ctx.wait(Duration::from_hours(2)).await?;

    ctx.wait(Duration::from_hours(24)).await?;

    ctx.wait(Duration::from_hours(1) + Duration::from_mins(30))
        .await?;

    Ok(())
}

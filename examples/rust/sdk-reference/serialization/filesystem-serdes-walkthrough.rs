use aws_durable_execution_sdk as durable;
use durable::serdes::FileSystemSerdes;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct CustomerEvent {
    customer_id: String,
}

#[derive(Serialize, Deserialize)]
struct Profile {
    name: String,
}

async fn handler(
    event: CustomerEvent,
    ctx: durable::DurableContext,
) -> Result<Profile, durable::BoxError> {
    let fs_serdes = FileSystemSerdes::new("/mnt/s3");

    // Pass the FileSystem serdes to one step. Other operations in this handler
    // keep using the default JSON serdes.
    let customer_id = event.customer_id;
    let profile = ctx
        .step(move |_| async move { Ok(load_large_profile(&customer_id)) })
        .name("load-profile")
        .serdes(fs_serdes)
        .await?;

    Ok(Profile { name: profile.name })
}

fn load_large_profile(customer_id: &str) -> Profile {
    Profile {
        name: format!("Customer {customer_id}"),
    }
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}

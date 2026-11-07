#[derive(Debug, Clone, Copy, PartialEq, Eq, Default)]
#[non_exhaustive]
pub enum JitterStrategy {
    None, // exact calculated delay
    Half, // random between 50% and 100% of base_delay
    #[default]
    Full, // random between 0 and base_delay
}

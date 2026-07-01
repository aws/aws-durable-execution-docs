public enum JitterStrategy
{
    None, // exact calculated delay
    Full, // random between 0 and base_delay
    Half  // random between 50% and 100% of base_delay
}

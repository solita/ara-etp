/**
 * Predicate function to check if patevyystaso ID is a basic level (1 or 2)
 * Used for filtering patevyystaso options when ETP 2026 feature flag is disabled
 */
export const isBasicPatevyystaso = id => id === 1 || id === 2;
package com.axibase.tsd.api.model.series.query.transformation.forecast;

/**
 * Type of svd using during forecast. NOISLESS - the type depends on forecast configuration and data.
 * FULL - execute full SVD and select desired number of eigentriples. TRUNCATED - partial SVD using
 * Lanczos algorithm with partial reorthogonalization.
 */
public enum SvdMethod {
    AUTO, FULL, TRUNCATED
}

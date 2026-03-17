package io.shortmesh.sdk

/**
 * Holds the endpoint URL(s) the SDK needs to call.
 *
 * Example:
 * ```
 * ShortMeshEndpoints(
 *     platforms = "https://example.com/api/v1/platforms"
 * )
 * ```
 */
data class ShortMeshEndpoints(
    /** GET — returns list of available platforms. */
    val platforms: String
)

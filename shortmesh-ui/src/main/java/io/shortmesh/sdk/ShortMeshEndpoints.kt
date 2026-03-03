package io.shortmesh.sdk

/**
 * Holds the four fully-qualified endpoint URLs the SDK must call.
 * No URL is ever hardcoded inside the SDK itself.
 *
 * Example:
 * ```
 * ShortMeshEndpoints(
 *     platforms = "https://example.com/api/v1/platforms",
 *     sendOtp   = "https://example.com/api/v1/otp/generate",
 *     verifyOtp = "https://example.com/api/v1/otp/verify",
 *     resendOtp = "https://example.com/api/v1/otp/resend"
 * )
 * ```
 */
data class ShortMeshEndpoints(
    /** GET — returns list of available platforms. */
    val platforms: String,
    /** POST — sends an OTP to the user. */
    val sendOtp: String,
    /** POST — verifies the OTP code entered by the user. */
    val verifyOtp: String,
    /** POST — re-sends the OTP after the countdown expires. */
    val resendOtp: String
)

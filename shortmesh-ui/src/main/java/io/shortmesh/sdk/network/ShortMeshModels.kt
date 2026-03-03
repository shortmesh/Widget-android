package io.shortmesh.sdk.network


data class PlatformDto(
    val platform: String,
    val sender: String
) {

    val id: String get() = platform


    val label: String get() = when (platform) {
        "wa"     -> "WhatsApp"
        "tg"     -> "Telegram"
        "signal" -> "Signal"
        else     -> platform.replaceFirstChar { it.uppercase() }
    }
}

data class GenerateOtpResponse(
    val success: Boolean = false,
    val message: String? = null,
    val expiresIn: Int? = null,
    val expiry: Int? = null,
    val ttl: Int? = null
) {
    /** Expiry in seconds, falling back to 30s if the server doesn't tell us. */
    val expirySeconds: Int get() = expiresIn ?: expiry ?: ttl ?: 30
}

data class VerifyOtpResponse(
    val verified: Boolean = false,
    val success: Boolean = false,
    val status: String? = null,
    val message: String? = null
) {

    val isVerified: Boolean get() = verified
            || success
            || status == "verified" || status == "ok" || status == "success"
            || message?.contains("verified", ignoreCase = true) == true
            || message?.contains("success", ignoreCase = true) == true
}

data class ResendOtpResponse(
    val success: Boolean
)


data class GenerateOtpRequest(
    val identifier: String,
    val platform: String
)

data class VerifyOtpRequest(
    val identifier: String,
    val platform: String,
    val code: String
)

data class ResendOtpRequest(
    val identifier: String,
    val platform: String
)

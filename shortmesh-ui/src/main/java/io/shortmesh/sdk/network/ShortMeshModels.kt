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

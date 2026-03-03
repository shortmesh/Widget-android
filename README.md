# ShortMesh Android SDK

A plug-and-play Android verification widget that guides users through a multi-platform OTP (one-time password) flow — WhatsApp, Telegram, Signal, and more — without you having to build any UI or networking yourself.

---

## Table of Contents

1. [How it works (overview)](#how-it-works-overview)
2. [Requirements](#requirements)
3. [Setup](#setup)
4. [Usage](#usage)
5. [Backend API contract](#backend-api-contract)
6. [Screen-by-screen walkthrough](#screen-by-screen-walkthrough)
7. [Error handling](#error-handling)
8. [Supported platforms](#supported-platforms)

---

## How it works (overview)

```
Your app
  │
  └─► ShortMeshWidget.launch(...)
          │
          ├─ 1. Fetches the list of platforms your backend supports
          │        GET /api/v1/platforms
          │
          ├─ 2. User picks a platform (e.g. WhatsApp)
          │
          ├─ 3. SDK requests an OTP for that platform
          │        POST /api/v1/otp/generate
          │
          ├─ 4. User types the code they received
          │
          ├─ 5. SDK verifies the code
          │        POST /api/v1/otp/verify
          │
          └─ 6. onSuccess() or onError() is called back in your app
```

The SDK handles **all UI, loading states, error messages, countdown timers, and retries**. You just supply the four endpoint URLs and react to the result.

---

## Requirements

| Requirement | Minimum |
|---|---|
| Android API level | 24 (Android 7.0) |
| Kotlin | 1.9+ |
| Jetpack Compose | included via the SDK |

---

## Setup

### 1. Add the module to your project

The SDK is distributed as a local Android library module. In your project's `settings.gradle.kts`, make sure the module is included:

```kotlin
include(":shortmesh-ui")
```

### 2. Add the dependency to your app

In your **app-level** `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":shortmesh-ui"))
}
```

### 3. Enable core library desugaring

The SDK requires Java 8+ APIs on older Android versions. Add the following to your **app-level** `build.gradle.kts`:

```kotlin
android {
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools.desugar_jdk_libs:2.1.4")
    // ... rest of your dependencies
}
```

### 4. Internet permission

Make sure your `AndroidManifest.xml` has the internet permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Usage

Call `ShortMeshWidget.launch(...)` from anywhere you have a `Context` — an Activity, a Fragment, or a Composable (via `LocalContext.current`).

### Kotlin example

```kotlin
import io.shortmesh.sdk.ShortMeshEndpoints
import io.shortmesh.sdk.ShortMeshWidget

ShortMeshWidget.launch(
    context    = this,                          // any Context
    identifier = "+237650393369",               // the phone number to verify
    endpoints  = ShortMeshEndpoints(
        platforms = "https://yourapi.com/api/v1/platforms",
        sendOtp   = "https://yourapi.com/api/v1/otp/generate",
        verifyOtp = "https://yourapi.com/api/v1/otp/verify",
        resendOtp = "https://yourapi.com/api/v1/otp/resend"  // currently re-uses sendOtp internally
    ),
    onSuccess  = {
        // Called on the main thread when the user is verified ✅
        Toast.makeText(this, "Verified!", Toast.LENGTH_SHORT).show()
    },
    onError    = { errorMessage ->
        // Called on the main thread if something goes wrong ❌
        Log.e("MyApp", "Verification failed: $errorMessage")
    }
)
```

### Jetpack Compose example

```kotlin
@Composable
fun MyScreen() {
    val context = LocalContext.current

    Button(onClick = {
        ShortMeshWidget.launch(
            context    = context,
            identifier = "+237650393369",
            endpoints  = ShortMeshEndpoints(
                platforms = "https://yourapi.com/api/v1/platforms",
                sendOtp   = "https://yourapi.com/api/v1/otp/generate",
                verifyOtp = "https://yourapi.com/api/v1/otp/verify",
                resendOtp = "https://yourapi.com/api/v1/otp/resend"
            ),
            onSuccess = { /* proceed */ },
            onError   = { error -> /* handle */ }
        )
    }) {
        Text("Verify my number")
    }
}
```

### `ShortMeshEndpoints` fields

| Field | HTTP method | Purpose |
|---|---|---|
| `platforms` | `GET` | Returns the list of platforms the user can choose from. |
| `sendOtp` | `POST` | Sends an OTP to the user via the chosen platform. |
| `verifyOtp` | `POST` | Validates the code the user entered. |
| `resendOtp` | `POST` | Re-sends the OTP after the countdown expires (currently calls the same endpoint as `sendOtp`). |

---

## Backend API contract

The SDK expects your backend to follow these request/response shapes.

### `GET /api/v1/platforms`

**Response** — JSON array:
```json
[
  { "platform": "wa",     "sender": "1234567890" },
  { "platform": "signal", "sender": "+1234567890" }
]
```

| Field | Description |
|---|---|
| `platform` | Short platform ID. Built-in labels: `wa` → WhatsApp, `tg` → Telegram, `signal` → Signal. Any other value is title-cased automatically. |
| `sender` | The number or handle that will send the OTP (displayed to the user). |

---

### `POST /api/v1/otp/generate`

**Request body:**
```json
{
  "identifier": "+1234567890",
  "platform": "wa"
}
```

**Response** — the SDK reads any of these fields for the expiry countdown:
```json
{
  "message": "OTP sent successfully",
  "expiresIn": 120
}
```

| Response field | Description |
|---|---|
| `expiresIn` | Seconds until the OTP expires (preferred). |
| `expiry` | Alternative name for the same value. |
| `ttl` | Another alternative. |

If none are present, the countdown defaults to **30 seconds**.

---

### `POST /api/v1/otp/verify`

**Request body:**
```json
{
  "identifier": "+1234567890",
  "platform": "wa",
  "code": "123456"
}
```

**Response** — the SDK accepts any of these as a success:
```json
{ "message": "OTP verified successfully" }
```

The SDK considers verification **successful** if any of the following is true:

- `verified: true`
- `success: true`
- `status` is `"verified"`, `"ok"`, or `"success"`
- `message` contains the word `"verified"` or `"success"` (case-insensitive)

---

## Screen-by-screen walkthrough

| Step | What the user sees |
|---|---|
| **1. Loading** | A spinner while the SDK fetches available platforms. |
| **2. Select platform** | Cards for each available platform (WhatsApp, Telegram, etc.). The user taps one and presses **Continue**. |
| **3. OTP entry** | A 6-digit code input, a countdown timer, and a **Resend** button that activates once the timer reaches zero. |
| **4. Success** | A confirmation screen with a **Done / Close** button. Stays visible until dismissed — it does **not** auto-close. |
| **5. Error** | If a fatal error occurs (e.g. no platforms returned, network failure), a full-screen error with a **Retry** button is shown. |

The widget appears as a **modal dialog** over your existing screen, so nothing in your own UI is replaced.

---

## Error handling

| Scenario | Behaviour |
|---|---|
| Network timeout | User sees a friendly error message with a retry option. |
| Backend returns HTML (wrong URL) | Detected automatically — shows "Unexpected response, check your endpoint URL." |
| HTTP 401 / 403 | Shows "Unauthorized. Check your API credentials." |
| HTTP 404 | Shows "Endpoint not found. Check your configured URLs." |
| HTTP 5xx | Shows "Server error. Please try again later." |
| OTP code wrong / expired | Error is shown inline on the OTP entry screen without leaving the screen. |
| No platforms returned | Fatal error screen with a **Retry** button. |

Errors that are recoverable (wrong OTP, network hiccup) are shown **inline** so the user can try again without losing their progress. Fatal errors (no platforms, completely unreachable server) show a dedicated error screen.

---

## Supported platforms

| `platform` value | Displayed as |
|---|---|
| `wa` | WhatsApp |
| anything else | Title-cased automatically (e.g. `viber` → `Viber`) |

---

## Troubleshooting

**"Dependency ':shortmesh-ui' requires core library desugaring"**  
→ Follow step 3 in [Setup](#setup).

**Widget shows "Unexpected response from server"**  
→ Your endpoint URL is probably returning an HTML error page. Double-check the URL you pass in `ShortMeshEndpoints`.

**OTP says "Invalid or expired" but the server returned 200**  
→ The SDK reads several fields (`verified`, `success`, `status`, `message`) from the JSON response. Make sure your backend's response body matches the contract in [Backend API contract](#backend-api-contract).

**The widget never moves past "Select platform"**  
→ Make sure `POST /otp/generate` returns a valid JSON response and that the `platform` field in the response matches what was sent.

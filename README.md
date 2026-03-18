# ShortMesh Android SDK

A lightweight, embeddable widget that lets users pick from available platforms to receive an authentication code. Drop it into any HTML page or React app — the widget handles the UI, you handle the rest.

---

![ShortMesh widget image](/1.svg)

---

## Table of Contents

1. [How it works](#how-it-works)
2. [Requirements](#requirements)
3. [Setup](#setup)
4. [Usage](#usage)
5. [Backend API contract](#backend-api-contract)
6. [Widget walkthrough](#widget-walkthrough)
7. [Error handling](#error-handling)
8. [Supported platforms](#supported-platforms)
9. [Troubleshooting](#troubleshooting)
10. [Integration Instructions](#integration-instructions)

---

## How it works

```
Your app
  │
  └─► ShortMeshWidget.launch(...)
          │
          ├─ 1. Fetches available platforms from your backend
          │        GET /api/v1/platforms
          │
          ├─ 2. User picks a platform (e.g. WhatsApp)
          │
          └─ 3. onPlatformSelected("wa") is called back in your app
```

The SDK handles all UI, loading states, and error messages. You supply the endpoint URL and react to the result.

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

The SDK ships as a local Android library module. In your project's `settings.gradle.kts`:

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

Add the following to your **app-level** `build.gradle.kts`:

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
}
```

### 4. Internet permission

The SDK declares this automatically via its own manifest. If your app has a custom network security config, also add it to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Usage

Call `ShortMeshWidget.launch(...)` from anywhere you have a `Context`.

### Kotlin / Activity example

```kotlin
import io.shortmesh.sdk.ShortMeshEndpoints
import io.shortmesh.sdk.ShortMeshWidget

ShortMeshWidget.launch(
    context            = this,
    endpoints          = ShortMeshEndpoints(
        platforms = "https://yourapi.com/api/v1/platforms"
    ),
    onPlatformSelected = { platform ->
        // platform is e.g. "wa", "tg", "signal"
        // use this to know where to send the OTP from your own backend
        Log.d("MyApp", "User selected: $platform")
    },
    onError = { errorMessage ->
        Log.e("MyApp", "Widget error: $errorMessage")
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
            context            = context,
            endpoints          = ShortMeshEndpoints(
                platforms = "https://yourapi.com/api/v1/platforms"
            ),
            onPlatformSelected = { platform -> /* send OTP via this platform */ },
            onError            = { error -> /* handle error */ }
        )
    }) {
        Text("Verify my number")
    }
}
```

### `ShortMeshEndpoints` fields

| Field | Type | Description |
|---|---|---|
| `platforms` | `String` | Full URL of your `GET /platforms` endpoint. |

---

## Backend API contract

### `GET /api/v1/platforms`

The SDK calls this on launch. It must return a JSON array of platform objects.

**Response:**
```json
[
  { "platform": "wa",     "sender": "123456789" }
]
```

| Field | Description |
|---|---|
| `platform` | Short platform ID. Built-in display names: `wa` → WhatsApp, `tg` → Telegram, `signal` → Signal. Any other value is title-cased automatically. |
| `sender` | The number or handle that will contact the user (for your reference). |

**Empty array:**
If the array is empty, the widget shows:
> *"No available verification methods. Contact support for assistance."*

---

## Widget walkthrough

| State | What the user sees |
|---|---|
| **Loading** | A spinner card with "Loading platforms…" |
| **Select platform** | A card listing all available platforms. The user taps one and presses **Select**. |
| **No platforms** | Inline message: "No available verification methods. Contact support for assistance." with a **Close** button. |
| **Error** | The error message with a **Retry** button. |

The widget appears as a **modal dialog** over your existing screen — nothing in your UI is replaced or navigated away from.

---

## Error handling

| Scenario | What the user sees |
|---|---|
| Network timeout | "The server took too long to respond. Check your endpoint URL and try again." |
| Server closes connection | "Server closed the connection without responding. Check your endpoint URL." |
| HTML returned (wrong URL) | "Unexpected HTML response. Check your endpoint URL." |
| HTTP 401 / 403 | "Unauthorized. Check your API key." |
| HTTP 404 | "Endpoint not found. Check your configured URL." |
| HTTP 5xx | "Server error (5xx). Please try again later." |
| Empty platform list | Shown inline — no error screen. |

All load errors show a card with a **Retry** button so the user can try again without closing the widget.

---

## Supported platforms

| `platform` value | Displayed as |
|---|---|
| `wa` | WhatsApp |

| anything else | Title-cased automatically (e.g. `viber` → `Viber`) |

[Authy](https://github.com/shortmesh/Authy-API) is working to add more platforms.

---

## Troubleshooting

**"Dependency ':shortmesh-ui' requires core library desugaring"**
→ Follow step 3 in [Setup](#setup).

**Widget shows "Unexpected HTML response"**
→ Your `platforms` URL is returning an HTML page (e.g. a 404 or login wall). Double-check the full URL.

**Widget shows "Server closed the connection without responding"**
→ The server is resetting the connection before sending any HTTP headers. The SDK uses HTTP/1.1 only — verify your server supports it.

**Widget always shows the error screen even with the correct URL**
→ Check the device has internet access, and that your server returns `Content-Type: application/json` with a valid JSON array.

---

## Integration Instructions

To integrate the ShortMeshSDK into your Android project, follow these steps:

1. **Download the AAR File**
   - Visit the [GitHub Releases page](https://github.com/shortmesh/Widget-android/releases) and download the latest `shortmesh-ui-release.aar` file.

2. **Add the AAR to Your Project**
   - Place the downloaded AAR file in your app module's `libs` directory.
   - Update your `build.gradle.kts` file to include the AAR:

     ```kotlin
     repositories {
         flatDir {
             dirs 'libs'
         }
     }

     dependencies {
         implementation(name: 'shortmesh-ui-release', ext: 'aar')
     }
     ```

3. **Sync Your Project**
   - Sync your Gradle project to ensure the AAR is included.

4. **ProGuard Configuration**
   - If you are using ProGuard, ensure the rules from the `consumer-rules.pro` file are included in your app's ProGuard configuration.

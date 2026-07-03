# KinderGuard — Android Child Monitoring App

Native Android app (Java) with a Firebase backend. Single APK, two roles selected at first launch: **Parent** and **Child**. Built from the SRS in `Requirements.pdf`.

## Status: built and signed

Both APKs are built and ready to install — no Android Studio needed:

- `../KinderGuardApp-APKs/KinderGuard-debug.apk` — debug build, install directly for testing.
- `../KinderGuardApp-APKs/KinderGuard-release.apk` — signed release build, this is the one to actually hand out to real parent/child devices.

Firebase project `kinderguard-fyp-cms` is fully live: Realtime Database has scoped security rules deployed (see `database.rules.json`), Email/Password and Google sign-in are enabled, and both the debug and release signing certificates are registered so Google Sign-In works on both APKs out of the box.

CI is also wired up: every push to `main` on [mindspire-org/KinderGuardApp](https://github.com/mindspire-org/KinderGuardApp) rebuilds the debug APK via `.github/workflows/build-apk.yml` and attaches it as a downloadable Actions artifact.

## 1. Signing key — keep this safe

The release APK is signed with `keystore/kinderguard-release.jks` (gitignored — **not** in the GitHub repo since it's public). Store password and key password: `KinderGuard2026!`, alias: `kinderguard`. **Back this file up somewhere private.** If you lose it, you can never publish an *update* to an already-installed release APK under the same signature — you'd have to uninstall and reinstall instead of upgrading in place.

## 2. Firebase

Already configured — nothing to do here. For reference:

- Project: `kinderguard-fyp-cms` (console login: `areebasohailfyp252@gmail.com`)
- Android app registered under package `com.kinderguard.app`
- Realtime Database rules: see `database.rules.json` — parents can only read/write their own node, children only their own (or their linked parent can), link codes can only be claimed by the UID that created them
- `app/google-services.json` is the real config (not a placeholder) and includes OAuth clients for both the debug and release signing certificates

If you ever need to regenerate `google-services.json` (e.g. after adding a new signing key), download it from Firebase Console → Project Settings → your Android app → `google-services.json`, or via `firebase apps:sdkconfig ANDROID <app-id>` with a CLI token.

## 3. Building from source yourself

Local Android SDK/JDK/Gradle used for this build (all portable, no system install required):

- JDK 17 (Temurin)
- Android SDK: `platform-tools`, `platforms;android-35`, `build-tools;35.0.0`
- Gradle 8.7 (no wrapper committed — the CI workflow installs Gradle directly via `gradle/actions/setup-gradle`)

To rebuild:

```
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/Android/Sdk
gradle assembleDebug assembleRelease
```

Or open the folder in Android Studio (Koala/2024.1+) — it'll provision its own JDK/SDK/Gradle wrapper automatically on first sync.

## 4. Installing on devices

- **Parent device**: install the release APK, launch, tap **Parent**, sign up, verify email, log in.
- **Child device**: install the same APK, launch, tap **Child**, sign up, verify email, log in. On first login the app asks for a **link code** — on the Parent device tap **Add Child** to generate one, then type it into the Child device's "Link to your Parent" screen (a "Skip for now" option exists for testing without a paired parent device). After linking, the permission wizard runs (location, background location, usage access, overlay, notifications, device admin, SMS/call access).

**This APK must be side-loaded (installed directly), not published to the Play Store.** SMS and Call Log permissions are restricted by Google Play policy to default SMS/dialer apps only; a parental-monitoring app requesting them will be rejected from the Store. Sideloading (sharing the `.apk` file directly, e.g. via a link or USB) is how real-world apps in this category (e.g. mSpy, FamiSafe outside their Play-compliant tiers) are distributed for the full feature set.

Since it's sideloaded, Android will warn about "unknown sources" on install — that's expected, not an error.

## 5. Known limitations / what to harden before real-world use

- **Unlock command is a no-op.** Stock Android has no public API to programmatically unlock a screen (by design, for security) — `Command.TYPE_UNLOCK` is a placeholder. `LOCK` works via Device Admin's `lockNow()`.
- **App blocking** is polling-based (checks the foreground app every few seconds via `UsageStatsManager`), not instantaneous — there can be a 1–4 second window before the block screen appears. A production version would use `AccessibilityService` for tighter enforcement.
- **No FCM push** — alerts (geofence, SOS, blocked-app) rely on the child's foreground service staying alive and Realtime Database listeners. This is battery-friendlier to build but less instant than push notifications when the child device is deep-sleeping; add Firebase Cloud Messaging if you need guaranteed delivery.
- **Battery optimization / OEM auto-kill**: on Xiaomi/Huawei/Oppo/etc., background services get killed aggressively regardless of "ignore battery optimization" settings. Real deployments typically need per-OEM instructions for the parent to whitelist the app manually in that vendor's battery settings.
- **This machine's C: drive was nearly full (under 1MB free at one point)** while building — this predates and is unrelated to this project, but is worth fixing since it risks the whole system, not just this build.

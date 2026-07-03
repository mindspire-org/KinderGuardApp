# KinderGuard — Android Child Monitoring App

Native Android app (Java) with a Firebase backend. Single APK, two roles selected at first launch: **Parent** and **Child**. Built from the SRS in `Requirements.pdf`.

## 1. Prerequisites

- Android Studio (Koala/2024.1 or newer)
- JDK 17 (bundled with recent Android Studio)
- Android SDK, platform 35, build-tools 35.x (Android Studio will prompt to install on first open)
- A Firebase project

This machine has no JDK/Android SDK/Gradle installed, so the project could not be compiled or turned into an `.apk` here. Everything below is written so you can do that in Android Studio.

## 2. Firebase setup

1. Go to https://console.firebase.google.com, sign in as `areebasohailfyp252@gmail.com`, create a project (e.g. "KinderGuard").
2. Add an Android app inside the project with package name exactly: `com.kinderguard.app`
3. Download the generated `google-services.json` and replace the placeholder file at `app/google-services.json` in this project (the placeholder has fake values and will make Firebase calls fail with "Invalid API key" until replaced).
4. In the Firebase console:
   - **Authentication → Sign-in method**: enable **Email/Password** and **Google**.
   - **Realtime Database**: create a database (choose a region), start in **test mode** for development, then apply the security rules below before any real use.
5. Recommended Realtime Database rules (adjust as needed — this restricts a child's data to that child and their linked parent, and a parent's profile to themselves):

```json
{
  "rules": {
    "parents": {
      "$parentUid": {
        ".read": "auth != null && auth.uid === $parentUid",
        ".write": "auth != null && auth.uid === $parentUid"
      }
    },
    "children": {
      "$childUid": {
        ".read": "auth != null && (auth.uid === $childUid || root.child('children').child($childUid).child('parentUid').val() === auth.uid)",
        ".write": "auth != null && (auth.uid === $childUid || root.child('children').child($childUid).child('parentUid').val() === auth.uid)"
      }
    },
    "linkCodes": {
      "$code": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

### Alternative: provisioning via Firebase CLI

If you'd rather not click through the console, run locally:

```
npx firebase-tools login:ci
```

This opens a browser, you approve as `areebasohailfyp252@gmail.com`, and prints a CI token. With that token you (or an agent given the token) can run `firebase projects:create`, `firebase apps:create ANDROID`, `firebase apps:sdkconfig ANDROID` (downloads `google-services.json` non-interactively), and `firebase database:update` to push the rules above — all without opening a browser again.

## 3. Opening and building

1. Open the `KinderGuardApp/` folder in Android Studio ("Open" → select this folder, the one containing `settings.gradle`).
2. Let Gradle sync (Android Studio manages its own Gradle wrapper automatically the first time you open a project without one).
3. Build → Make Project to confirm it compiles.
4. Build → Generate Signed Bundle / APK → APK → create a new keystore (or use an existing one) → build **release** (or just **debug** for testing, no signing needed).
5. The output APK lands in `app/build/outputs/apk/debug/app-debug.apk` (or `release/...`).

Command-line equivalent once Android Studio has generated the Gradle wrapper:

```
./gradlew assembleDebug
```

## 4. Installing on devices

- **Parent device**: install the APK, launch, tap **Parent**, sign up, verify email, log in.
- **Child device**: install the same APK, launch, tap **Child**, sign up, verify email, log in. On first login the app asks for a **link code** — on the Parent device tap **Add Child** to generate one, then type it into the Child device's "Link to your Parent" screen (a "Skip for now" option exists for testing without a paired parent device). After linking, the permission wizard runs (location, background location, usage access, overlay, notifications, device admin, SMS/call access).

**This APK must be side-loaded (installed directly), not published to the Play Store.** SMS and Call Log permissions are restricted by Google Play policy to default SMS/dialer apps only; a parental-monitoring app requesting them will be rejected from the Store. Sideloading (sharing the `.apk` file directly, e.g. via a link or USB) is how real-world apps in this category (e.g. mSpy, FamiSafe outside their Play-compliant tiers) are distributed for the full feature set.

## 5. Known limitations / what to harden before real-world use

- **Unlock command is a no-op.** Stock Android has no public API to programmatically unlock a screen (by design, for security) — `Command.TYPE_UNLOCK` is a placeholder. `LOCK` works via Device Admin's `lockNow()`.
- **App blocking** is polling-based (checks the foreground app every few seconds via `UsageStatsManager`), not instantaneous — there can be a 1–4 second window before the block screen appears. A production version would use `AccessibilityService` for tighter enforcement.
- **No FCM push** — alerts (geofence, SOS, blocked-app) rely on the child's foreground service staying alive and Realtime Database listeners. This is battery-friendlier to build but less instant than push notifications when the child device is deep-sleeping; add Firebase Cloud Messaging if you need guaranteed delivery.
- **Security rules above are a starting point**, not audited — review before handling real children's data.
- **Battery optimization / OEM auto-kill**: on Xiaomi/Huawei/Oppo/etc., background services get killed aggressively regardless of "ignore battery optimization" settings. Real deployments typically need per-OEM instructions for the parent to whitelist the app manually in that vendor's battery settings.

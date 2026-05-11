# Privacy Audit Results

## Executive Summary
No evidence was found that the application collects or sends unique device identifiers (Device ID, IMEI, Mac Address), device fingerprints, or detailed network information to Reddit or any third-party analytics services.

## Detailed Findings

1. **Device Identifiers**
   - **Search:** Scanned codebase for `ANDROID_ID`, `TelephonyManager.getDeviceId`, `WifiManager.getConnectionInfo`, `Build.SERIAL`.
   - **Result:** No matches found. The app does not access these sensitive identifiers.

2. **Device Fingerprinting**
   - **Search:** Scanned for usage of `Build` class fields (Manufacturer, Model, etc.).
   - **Result:** `Build.MANUFACTURER` and `Build.MODEL` are used **only** in `CrashReportsFragment.java`.
   - **Context:** This data is used to pre-fill a GitHub issue template when the user *manually* chooses to "Export Logs". It is not collected in the background.

3. **Network Information**
   - **IP Address:** The app does not explicitly fetch the device's IP address.
   - **Note:** Communicating with Reddit's servers (or any server) inherently reveals the device's public IP address to that server, which is standard internet behavior.

4. **Analytics & Tracking**
   - **Search:** Checked `build.gradle` for Firebase, Google Analytics, Crashlytics, etc.
   - **Result:** No major analytics SDKs are present.
   - **Crash Reporting:** The app uses `com.github.FunkyMuse:Crashy`, a local crash reporting library. Logs are stored locally and only shared if the user manually exports them.

5. **User-Agent**
   - **Value:** `android:ml.docilealligator.infinityforreddit: (by /u/Hostilenemy)`
   - **Analysis:** This string is static and generic. It does not contain any unique device information.

## Conclusion
The application appears to be privacy-friendly regarding device identifiers and tracking. It functions as a standard API client without background data collection.

---
*Audit performed by Gemini CLI on 2025-12-24*

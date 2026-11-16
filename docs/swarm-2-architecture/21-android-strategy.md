# PURPOSE: Operator/developer documentation for SWARM.
# 21 — Android strategy

## Exists today

Capacitor wraps the React SPA (`com.nanofarm.swarm`). Plugins: core, app, browser. JWT in **localStorage**. Native SSO via `@capacitor/browser` + `appUrlOpen`. Global cleartext HTTP for ESP pairing. No offline. Back button: history then `exitApp()`.

This is the right product shape: operators get Field Devices, dashboard, alerts, NOVA — not a separate Kotlin app. The NOVA DNA Android chat app is **out of scope**.

## Target

| Topic | Decision |
|---|---|
| Codebase | Same SPA |
| Auth | Same access/refresh as web; store refresh in Android Keystore (plugin slice) |
| SSO | Keep deep link; emPOWER must register redirect |
| Pairing | Keep browser→LAN hub; restrict cleartext to 10/8, 172.16/12, 192.168/16, 169.254/16, 192.168.4.1 |
| Offline | Phase 2: cache last dashboard + open alerts read-only; queue shift notes; **never** queue HMI commands |
| Push | After notifications exist; FCM optional |
| API | `VITE_API_URL` baked at build (`docs/ANDROID_APK.md`) |

## Security

- Logout clears WebView storage + preferences.
- Certificate pinning later if Cloudflare MITM is a concern — not required for 2.0 start.
- Screenshot of tokens in Help should not include secrets.

## UX

Field Devices pairing copy already matches on-site Wi-Fi. Keep large targets. Do not hide Pair behind a desktop-only sidebar without the bottom nav on small screens (`MainLayout` already has a mobile drawer — verify pairing forms `responsiveSelect`).

## Acceptance

- APK can log in (local or SSO).
- APK can pair a hub on plant Wi-Fi.
- APK cannot reach arbitrary cleartext WAN IPs through the WebView.
- Killing the app does not leave an unexpired 24 h bearer as the only credential (after session work).

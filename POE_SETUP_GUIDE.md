# Winx Final POE — Beginner setup guide

## Open the project
1. Extract the ZIP using Windows File Explorer (right-click > Extract All).
2. Install Android Studio and the Android SDK. Install the .NET 10 SDK for the backend.
3. Android Studio > Open > select the extracted folder containing `settings.gradle.kts`.
4. Wait for Gradle Sync. Choose the **app** run configuration.
5. On your Android phone, enable Developer options (tap Build number seven times) and USB debugging. Connect USB, allow the debugging prompt, and select the phone in Android Studio.

## Start the existing backend
Open a terminal in `Winx.API` and run `dotnet restore`, then `dotnet run`. Visit the Swagger URL printed in the terminal. Apply EF migrations if required (`dotnet ef database update`).

## Physical phone networking
The emulator-only address `10.0.2.2` will NOT work on a phone. For a development build, put the computer and phone on the same Wi-Fi, run the API on an interface reachable from the phone, and build with `-PWINX_API_BASE_URL=http://YOUR_PC_LAN_IP:5001/`. Permit the API port in Windows Firewall. For a release, use a publicly reachable HTTPS API endpoint instead.

## Firebase notifications
Use your own Firebase project with the Android package name `com.winx.app`. Download `google-services.json` into `app/`. In Firebase Console > Messaging, send a test notification to a registered device. Android 13+ requires the app to request POST_NOTIFICATIONS at runtime; receiving service alone does not grant permission. The final production solution also needs token registration and a secure server-side sender.

## CI and GitHub
Create a GitHub repository, upload source folders (not ZIP), and push. Under Actions, verify both Android CI and API CI show green checks. Do not commit signing keys, access tokens, database passwords or private service-account files. The final commit must be tagged `Final-POE` (Git tags cannot contain spaces): `git tag -a Final-POE -m "Final POE"` then `git push origin Final-POE`.

## Release build
Android Studio > Build > Generate Signed Bundle / APK > APK. Create and safely back up a private keystore, choose release, and build. Never commit the keystore. Test the signed APK on a real Android phone. Capture screenshots on the device and add them to a `docs/screenshots` folder. Add the final video URL to README when available.

## Verification
This working copy is NOT yet a complete POE submission. Read RELEASE_NOTES.md for remaining implementation work. Run Gradle and .NET builds on your computer and resolve any SDK or plugin compatibility issues before committing.

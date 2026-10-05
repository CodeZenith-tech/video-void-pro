# VideoVoid Pro

Android phone-only public-video downloader using yt-dlp-android 2.0.2.

## What this build does
- Uses the current yt-dlp Android library to resolve supported public video URLs.
- Instagram, YouTube, Facebook, TikTok and other supported extractors are handled by yt-dlp rather than a fake URL detector.
- Real callback progress drives the UI from 0% toward 100%.
- Selectable quality: Best, 2160p, 1440p, 1080p, 720p, 480p, Audio only.
- Saves to `Downloads/VideoVoid`.
- No downloader-added watermark is applied. If the source itself contains a watermark, this app does not erase the creator/platform watermark.
- Does not bypass DRM, private content, or access controls.

## GitHub APK build
Upload the repository to GitHub. Actions will build `app-debug.apk` and publish it as `VideoVoid-debug-apk`.

## Important platform reality
Instagram extraction can fail when a post is private, unavailable without login, or when the platform changes its delivery/authentication behavior. The app uses yt-dlp's current extractor instead of claiming universal 100% compatibility.

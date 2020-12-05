Raccoon - PC APK Downloader
---------------------------

Raccoon is an APK downloader for fetching apps from Google Play.

* Cross platform (Linux, Windows, macOS)
* Avoids the privacy issues arising from connecting your Android device 
  with a Google account.
* Easily install apps on multiple devices without downloading them several
  times.

Building
--------

Raccoon is built with Gradle. It is recommended to use the "launch4j" task
instead of the standard one. Also, the version of the build must be submitted
via the "version" property, e.g:

gradlew -Pversion=4.x.y-DEV createExe

Prebuilt binaries are available at

https://raccoon.onyxbits.de


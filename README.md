Raccoon - PC APK Downloader
---------------------------

Raccoon is an APK downloader for fetching apps from Google Play.

* Cross platform (Linux, Windows, Mac OS)
* Avoids the privacy issues that arise from connecting your Android device 
  with a Google account
* Easily install apps on multiple devices without downloading them several
  times.

Building
--------

Raccoon is build with gradle. It is recommended to use the "launch4j" task
instead of the standard one. Also, the version of the build must be submitted
via the "version" property, e.g.:

gradlew -Pversion=4.x.y-DEV createExe

Prebuild binaries are available at

https://raccoon.onyxbits.de


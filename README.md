Raccoon - Google Play Desktop client
------------------------------------

Raccoon is the method of choice for downloading apps from Google Play for
anyone who either doesn't have GAPPS enabled device or doesn't want to use 
GAPPS to begin with.


Building
--------

Raccoon is build with gradle. It is recommended to use the "launch4j" task
instead of the standard one. Also, the version of the build must be submitted
via the "version" property, e.g.:

gradlew -Pversion=4.x.y-DEV launch4j


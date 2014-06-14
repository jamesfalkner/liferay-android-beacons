# Android iBeacon Library for Titanium Appcelerator

This project is a module for Titanium Appcelerator, which allows Titanium apps that support Android to
interact with iBeacons via an easy to use Java API.

Apps can be notified of entering/exiting iBeacon regions, and can enable ranging to receive periodic beacon
proximity reports.

## Getting and using the module

This module can be installed and used in Titanium projects like any other module. To download the latest
binary module, check out the `dist/` directory in this project.

Once installed, [read the documentation](https://github.com/jamesfalkner/liferay-android-beacons/blob/master/documentation/index.md) (it lives in the `documentation/` directory) to learn how to interact with your iBeacons.

## Requires for building

If you wish to build this module yourself, you will need a Titanium+Android build environment consisting of:

* [Titanium CLI](http://docs.appcelerator.com/titanium/3.0/#!/guide/Titanium_Command-Line_Interface_Reference) to create module projects (since Release 3.3.0).
* [Titanium Mobile SDK 3.2.0 or later](http://www.appcelerator.com/titanium/titanium-sdk/)
* All of the [prerequisites for developing Android applications](https://developer.android.com/sdk/index.html).
* [Android NDK Release 9.d or later](https://developer.android.com/tools/sdk/ndk/index.html)
* [Ant 1.7.1](http://ant.apache.org) or above must be installed and in your system PATH to build from the command line.
* [gperf must be installed](http://docs.appcelerator.com/titanium/3.0/#!/guide/Installing_gperf) and in your system PATH.

## How to build

To build this project:

* Fork a copy
* Edit the `build.properties` file and change each setting therein to point to your local copy of Titanium, Android SDK, and Android NDK. Here is an example of the paths this author uses on a Mac OS X system:

```
	titanium.platform=/Users/jhf/Library/Application Support/Titanium/mobilesdk/osx/3.2.2.GA/android
	android.platform=/Users/jhf/androidsdk/platforms/android-10
	android.ndk=/Users/jhf/androidndk
	google.apis=/Users/jhf/androidsdk/add-ons/addon-google_apis-google-10
```
 
* Run `ant dist`. This creates the distribution in the `dist/` directory of the project.

Other targets supported by Titanium include:

* `ant clean` Removes all generated zips and binaries from previous builds.
* `ant install` Runs the *dist* build target, generates a test project using `example/` as the *Resources*, and then installs it to a connected Android device.
* `ant run.emulator` Launches an Android emulator for the *run* build target.
* `ant run` Runs the *dist* build target, generates a test project using `example/` as the *Resources*, and then installs it to a running emulator (hint: use the *run.emulator* target to start up an emulator!).

## Author

![James Falkner Logo](https://cdn.lfrs.sl/www.liferay.com/image/user_male_portrait?img_id=6182018&t=1402762276765)

* James Falkner (Liferay Community Manager)
* `james.falkner@liferay.com`
* [`@schtool`](http://twitter.com/schtool)


## License

Copyright (c) 2014, Liferay Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Notice

This product includes software developed at
[The Radius Networks](http://www.radiusnetworks.com) (http://www.radiusnetworks.com/).

Android IBeacon Service

Copyright 2013 Radius Networks

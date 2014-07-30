# liferay.beacons Module

## Description

A Titanium module to interact with iBeacons in Titanium projects that support Android.

## Accessing the liferay.beacons Module

Place the ZIP file into your project's root directory, and declare the module and required android permissions in your `tiapp.xml` file (or in your custom `platform/android/AndroidManifest.xml` file if you are using that):

	<ti:app>
		...
		<android xmlns:android="http://schemas.android.com/apk/res/android">
			<manifest package="[YOUR_APP_PACKAGE_NAME]">
				<uses-sdk	android:minSdkVersion="10"
							android:targetSdkVersion="18"/>
				<uses-permission
					android:name="android.permission.BLUETOOTH"/>
				<uses-permission
					android:name="android.permission.BLUETOOTH_ADMIN"/>
				<application>
					<service	android:enabled="true"
								android:exported="true"
								android:isolatedProcess="false"
								android:label="iBeacon"
								android:name="com.radiusnetworks.ibeacon.service.IBeaconService">
					</service>
					<service	android:enabled="true" 
								android:name="com.radiusnetworks.ibeacon.IBeaconIntentProcessor">
								<meta-data android:name="background" android:value="true" />
						<intent-filter 
							android:priority="1" >
							<action android:name="[YOUR_APP_PACKAGE_NAME].DID_RANGING"/>
							<action android:name="[YOUR_APP_PACKAGE_NAME].DID_MONITORING"/>
						</intent-filter>
					</service>
				</application>
			</manifest>
		</android>
		...
		<modules>
			<module platform="android">com.liferay.beacons</module>
		</modules>
		...
	</ti:app>

Don't forget to replace the `[YOUR_APP_PACKAGE_NAME]` with your app's package name, e.g. *com.companyname.app*, and you can read [Radius Networks' docs](http://developer.radiusnetworks.com/ibeacon/android/configure.html) on this topic as well.

Next, to access this module from JavaScript, you would do the following:

	var TiBeacons = null;
	if (Ti.Platform.name == "android") {
		TiBeacons = require("com.liferay.beacons");
	} else {
	  console.log("liferay.beacons not supported on " + Ti.Platform.name);
	}

Note that this library is only available for the Android platform. Attempting to use it on other platforms
will fail in different ways and *people will point and laugh at you*.

## Using the iBeacons API

This module enables Titanium projects to start/stop monitoring for beacon region events (enter/exit/determineState),
as well as ranging events (proximity). You can configure the beacon scan periods (to adjust battery usage),
and can enable or disable auto-ranging (when auto-ranging is enabled, then ranging will be turned on when a
region is entered, and turned off when the region is exited).

Note that there are not separate listeners for *proximity* vs *range* events. You can use *proximity* events alone to determine ranging, as the same data is included in each event.

### Setting up and starting to monitor and/or range

A typical workflow for beacons, and the corresponding JavaScript APIs for this module:

1. Get a reference to the module via

	`var TiBeacons = require('com.liferay.beacons');`

2. See if it's supported on the device via `TiBeacons.checkAvailability()` - If it is not, you should not attempt to call any other APIs, and somehow indicate that it's not supported in your app to the end user.

3. Decide whether you want auto-ranging, and turn it on via `TiBeacons.setAutoRange(true)` if you want it, or `TiBeacons.setAutoRange(false)` if not.

4. Attach listeners for region and range events

```
	TiBeacons.addEventListener("enteredRegion", handleRegionEnter);
	TiBeacons.addEventListener("exitedRegion", handleRegionExit);
	TiBeacons.addEventListener("determinedRegionState", handleRegionDeterminedState);
	TiBeacons.addEventListener("beaconProximity", handleProximityEvent);
```

You can also remove event listeners using the `TiBeacons.removeEventListener()`, for example:

    TiBeacons.removeEventListener("enteredRegion", handleRegionEnter);

5. Begin monitoring one or more regions

```

	TiBeacons.startMonitoringForRegion({
	  identifier: 'Region by UUID only',
	  uuid: '11111111-2222-3333-4444-555555555555'
	});
	
	TiBeacons.startMonitoringForRegion({
	  identifier: 'Region by UUID and major',
	  uuid: '11111111-2222-3333-4444-555555555555',
	  major: 2112
	});
	
	TiBeacons.startMonitoringForRegion({
	  identifier: 'Region by UUID and major and minor',
	  uuid: '11111111-2222-3333-4444-555555555555',
	  major: 2112,
	  minor: 73
	});

```

Once this is called, when the device enters or exits a region, the corresponding listener's callback will be called.

If autoranging is enabled, then the moment a region is entered, ranging (which is more expensive in terms of power) begins, and listener callbacks will be called for those as well.

If autoranging is NOT enabled, you must manually begin ranging (if you are interested in proximity/range events) like this:

	TiBeacons.startRangingForRegion({
	  identifier: 'Region by UUID only',
	  uuid: '11111111-2222-3333-4444-555555555555'
	});

### Stopping monitoring/ranging

To turn everything off:

    TiBeacons.stopRangingForAllBeacons();
    TiBeacons.stopMonitoringAllRegions();
    
### Objects passed to the callbacks

When one of your registered listeners' callbacks is called, they will receive different kinds of objects. Here are examples that print out all of the values received by each of your callbacks:

#### enteredRegion

	function enteredRegionCallback(e) {
		console.log("identifer: " + e.identifier);
	}

#### exitedRegion

	function exitedRegionCallback(e) {
		console.log("identifer: " + e.identifier);
	}

#### determinedRegionState

State can be either `inside` or `outside`. If the state is determined to be *unknown* then the callback will not be called.

	function determinedRegionStateCallback(e) {
		console.log("identifer: " + e.identifier);
		
		// it's either 'inside' or 'outside'
		console.log("regionState: " + e.regionState);
	}

#### beaconProximity

	function beaconProximityCallback(e) {
		console.log("identifer: " + e.identifier);
		console.log("uuid: " + e.uuid);
		console.log("major: " + e.major);
		console.log("minor: " + e.minor);
		console.log("proximity: " + e.proximity);
		console.log("accuracy: " + e.accuracy);
		console.log("rssi: " + e.rssi);
		console.log("power: " + e.power);
	}

Note that the proximity could be one of `immediate`, `near`, `far`, or `unknown`. See the [Radius Networks' docs](http://developer.radiusnetworks.com/android-ibeacon-service/doc/com/radiusnetworks/ibeacon/IBeacon.html) for more detail about accuracy, rssi, and power values given in the callback object.

### Foreground vs. Background

It is is a good idea for apps to reduce their power consumption when placed in the background by
a user of an android device (e.g. when they press the Home button to send an app to the background, but
do not hard-close the app).

To that end, this module can be configured with different scan periods for foreground vs. background modes,
however **this module DOES NOT DETECT when your app is sent to the background or brought back to the foreground**.
You must manually detect foreground/background events and call the appropriate APIs on this module to tell it
that it is now in the background and should use the background scan periods. Check out [Ben Bahrenburg's excellent
Android Tools](https://github.com/benbahrenburg/benCoding.Android.Tools) for a super-easy way to auto-detect this. Here's an example:

        var androidPlatformTools = require('bencoding.android.tools').createPlatform();
        var isForeground = androidPlatformTools.isInForeground();
        console.log("Am I currently in the foreground? " + isForeground);

You can call this repeatedly (e.g. every 5 seconds) using `setInterval()` and when foreground vs. background is detected, call `TiBeacons.setBackgroundMode()`. At least that's what I do.

To configure the scan periods for foreground and background:

	var TiBeacons = require('com.liferay.beacons');
	TiBeacons.setScanPeriods({
	  foregroundScanPeriod: 1000,
	  foregroundBetweenScanPeriod: 2000,
	  backgroundScanPeriod: 5000,
	  backgroundBetweenScanPeriod: 60000
	});

This says that when the module is in "foreground mode" (set via `TiBeacons.setBackgroundMode(false);` when foreground
is detected), then the device will scan for iBeacons for 1000ms, then wait 2000ms, then repeat. When in background mode (set via
`TiBeacons.setBackgroundMode(true);` when the app is sent to the background), it will scan for iBeacons for 5000ms,
followed by a 60000ms wait, and repeat.

Check out [the source code to the underlying Radius Networks module](https://github.com/RadiusNetworks/android-ibeacon-service/blob/master/src/main/java/com/radiusnetworks/ibeacon/service/IBeaconService.java) for a longer discussion on the best values to use,
and the defaults.

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
[The Radius Networks](http://www.radiusnetworks.com/) (http://www.radiusnetworks.com/).

Android IBeacon Service

Copyright 2013 Radius Networks

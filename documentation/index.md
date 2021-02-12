# liferay.beacons Module

## Description

A Titanium module to interact with iBeacons in Titanium projects that support Android.

## Accessing the liferay.beacons Module

Place the ZIP file into your project's root directory, and declare the module and required android permissions in your `tiapp.xml` file (or in your custom `platform/android/AndroidManifest.xml` file if you are using that):

```
<ti:app>
  ...
  <android xmlns:android="http://schemas.android.com/apk/res/android">
    <manifest package="[YOUR_APP_PACKAGE_NAME]">
      <uses-permission
        android:name="android.permission.BLUETOOTH"/>
      <uses-permission
        android:name="android.permission.BLUETOOTH_ADMIN"/>
      <uses-permission
        android:name="android.permission.ACCESS_FINE_LOCATION"/>
      <uses-permission
        android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
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
```

Don't forget to replace the `[YOUR_APP_PACKAGE_NAME]` with your app's package name, e.g. *com.companyname.app*, and you can read [Radius Networks' docs](http://altbeacon.github.io/android-beacon-library/configure.html) on this topic as well.

Next, to access this module from JavaScript, you would do the following:

```
var TiBeacons = null;
if (Ti.Platform.name === "android") {
  TiBeacons = require("com.liferay.beacons");
} else {
  console.log("liferay.beacons not supported on " + Ti.Platform.name);
}
```

As of Android 6.0, your app will need to request permission after launch in the form of a popup. This will need to be accepted by the user or else the service will fail. You can request permisison using an approach simialr to the below.

```
var permissions = ['android.permission.ACCESS_FINE_LOCATION'];
Ti.Android.requestPermissions(permissions, function(e) {
  if (e.success) {
    Ti.API.info("SUCCESS");
  } else {
    Ti.API.info("ERROR: " + e.error);
  }
});
```

Note that this library is only available for the Android platform. Attempting to use it on other platforms
will fail in different ways and *people will point and laugh at you*.

## Using the iBeacons API

This module enables Titanium projects to start/stop monitoring for beacon region events (enter/exit/determineState),
as well as ranging events (proximity). You can configure the beacon scan periods (to adjust battery usage),
and can enable or disable auto-ranging (when auto-ranging is enabled, then ranging will be turned on when a
region is entered, and turned off when the region is exited).

Note there are *two* ranging events that are produced from this module: `beaconProximity` and `beaconRanges`. In most cases
you will only attach listeners for one of these, because they tell you almost the same information. Read below to find out more.

### Setting up and starting to monitor and/or range

A typical workflow for beacons, and the corresponding JavaScript APIs for this module:

1. Get a reference to the module via

```
var TiBeacons = require('com.liferay.beacons');
```

**Note** that when Titanium evaluates the `require()` statement, it will immediately return from it while the module sets up the native BLE binding asynchronously. This means, for example, that you should not attempt to call `startMonitoringForRegion()` or `startRangingForRegion()` immediately after the call to `require()`. Instead, call them in a UI callback (e.g. when a button is clicked as part of an event handler, or when a specific window is opened). If you attempt to begin ranging or monitoring immediately after `require()`ing the module, you'll likely get an error such as

```
android.os.RemoteException: The IBeaconManager is not bound to the service. Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()
```

Instead of guessing when the service is ready, we can check using the following method adn force the bind:

```
var handle;
TiBeacons.bindBeaconService(); // This will force the bind to prevent TiBeacons.isReady() from always remaining false
handle = setInterval(function(){
    if(!TiBeacons.isReady())
        return;

    Ti.API.info("Okay! Module is ready!");  
    clearInterval(handle);
    handle = null;

    //setup your event listeners here
}, 1000);
```

2. See if it's supported on the device via `TiBeacons.checkAvailability()` - If it is not, you should not attempt to call any other APIs, and somehow indicate that it's not supported in your app to the end user. The module

3. Decide whether you want auto-ranging, and turn it on via `TiBeacons.setAutoRange(true)` if you want it, or `TiBeacons.setAutoRange(false)` if not. The default is `true` (that is, auto-ranging is enabled).

4. Attach listeners for region and range events

```
TiBeacons.addEventListener("enteredRegion", handleRegionEnter);
TiBeacons.addEventListener("exitedRegion", handleRegionExit);
TiBeacons.addEventListener("determinedRegionState", handleRegionDeterminedState);

/* You probably only want one of these */
TiBeacons.addEventListener("beaconProximity", handleProximityEvent);
TiBeacons.addEventListener("beaconRanges", handleRanges);
```

You can also remove event listeners using the `TiBeacons.removeEventListener()`, for example:

```
TiBeacons.removeEventListener("enteredRegion", handleRegionEnter);
```

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

```
TiBeacons.startRangingForBeacons({
  identifier: 'Region by UUID only',
  uuid: '11111111-2222-3333-4444-555555555555'
});
```

### Stopping monitoring/ranging

To turn everything off:

```
TiBeacons.stopRangingForAllBeacons();
TiBeacons.stopMonitoringAllRegions();
TiBeacons.unbindBeaconService(); // to force unbind
```

### Objects passed to the callbacks

When one of your registered listeners' callbacks is called, they will receive different kinds of objects. Here are examples that print out all of the values received by each of your callbacks:

#### enteredRegion

```
function enteredRegionCallback(e) {
  console.log("identifer: " + e.identifier);
}
```

#### exitedRegion

```
function exitedRegionCallback(e) {
  console.log("identifer: " + e.identifier);
}
```

#### determinedRegionState

State can be either `inside` or `outside`. If the state is determined to be *unknown* then the callback will not be called.

```
function determinedRegionStateCallback(e) {
  console.log("identifer: " + e.identifier);

  // it's either 'inside' or 'outside'
  console.log("regionState: " + e.regionState);
}
```

#### beaconProximity

```
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
```

Note that the proximity could be one of `immediate`, `near`, `far`, or `unknown`. See the [Radius Networks' docs](http://altbeacon.github.io/android-beacon-library/distance-calculations.html) for more detail about accuracy, rssi, and power values given in the callback object.

#### beaconRanges

This event payload contains the same data as the `beaconProximity` payload, except this event is only fired once
per hardware scan cycle, and the event contains an *array* of beacons in its payload, so that you can know how many beacons were scanned
during the scan period.

For example, if during a scan period, 7 beacons were ranged, then the `beaconProximity` event will be fired 7 times in a row, once for each ranged beacon,
and then the `beaconRanges` event will be fired *once*, with an array of the 7 beacons as part of its payload.

You normally only need to listen for `beaconProximity` *or* `beaconRanges`. You can listen for both if you like!

Also note that the order of the beacons in the array of the `beaconRanges` event is not guaranteed to be in any particular order across callbacks.

```
function beaconRangingCallback(e) {

  console.log("I am in the " + e.identifier + " region");
  console.log("I see " + e.beacons.length + " beacons in this region:");
  console.log("----------------");

  e.beacons.forEach(function(beacon, index) {
      console.log("Beacon number: " + index);
      console.log("uuid: " + beacon.uuid);
      console.log("major: " + beacon.major);
      console.log("minor: " + beacon.minor);
      console.log("proximity: " + beacon.proximity);
      console.log("accuracy: " + beacon.accuracy);
      console.log("rssi: " + beacon.rssi);
      console.log("power: " + beacon.power);
      console.log("----------------");
    }
}
```

### Foreground vs. Background

It is is a good idea for apps to reduce their power consumption when placed in the background by
a user of an android device (e.g. when they press the Home button to send an app to the background, but
do not hard-close the app).

To that end, this module can be configured with different scan periods for foreground vs. background modes,
however **this module DOES NOT DETECT when your app is sent to the background or brought back to the foreground**.
You must manually detect foreground/background events and call the appropriate APIs on this module to tell it
that it is now in the background and should use the background scan periods. Check out [Ben Bahrenburg's excellent
Android Tools](https://github.com/benbahrenburg/benCoding.Android.Tools) for a super-easy way to auto-detect this. Here's an example:
```
var androidPlatformTools = require('bencoding.android.tools').createPlatform();
var isForeground = androidPlatformTools.isInForeground();
console.log("Am I currently in the foreground? " + isForeground);
```
You can call this repeatedly (e.g. every 5 seconds) using `setInterval()` and when foreground vs. background is detected, call `TiBeacons.setBackgroundMode()`. At least that's what I do.

To configure the scan periods for foreground and background:
```
var TiBeacons = require('com.liferay.beacons');
TiBeacons.setScanPeriods({
  foregroundScanPeriod: 1000,
  foregroundBetweenScanPeriod: 2000,
  backgroundScanPeriod: 5000,
  backgroundBetweenScanPeriod: 60000
});
```
This says that when the module is in "foreground mode" (set via `TiBeacons.setBackgroundMode(false);` when foreground
is detected), then the device will scan for iBeacons for 1000ms, then wait 2000ms, then repeat. When in background mode (set via
`TiBeacons.setBackgroundMode(true);` when the app is sent to the background), it will scan for iBeacons for 5000ms,
followed by a 60000ms wait, and repeat.

Check out [the source code to the underlying Radius Networks module](https://github.com/AltBeacon/android-beacon-library/blob/master/src/main/java/org/altbeacon/beacon/service/BeaconService.java) for a longer discussion on the best values to use,
and the defaults.

## Example `app.js` for testing

Here is a simple `app.js` application that you can use to see if things are working. You may need to modify it a bit to align with your specific beacon UUID.

```
// sample Titanium app.js app to test that things are working,
// this assumes your hardware supports BLE and it's switched on.
// you can use checkAvailability() to see if it's supported, but
// we don't do that here just because we're lazy.

var TiBeacons = require('com.liferay.beacons');

// make a window with two buttons to start and stop monitoring
var win = Titanium.UI.createWindow({
    title:'iBeacon Test',
    backgroundColor:'#fff'
});

var b1 = Titanium.UI.createButton({
	title: "Start Monitoring"
});
var b2 = Titanium.UI.createButton({
	title: "Stop Monitoring"
});

var entered = function(reg) {
	alert("entered region: " + reg.identifier);
};

var exited = function(reg) {
	alert("exited region: " + reg.identifier);
};

b1.addEventListener('click', function(e) {

	// add the listeners for beacon region monitoring
    TiBeacons.addEventListener("enteredRegion", entered);
    TiBeacons.addEventListener("exitedRegion", exited);

    // start monitoring in the button click callback
    TiBeacons.startMonitoringForRegion({
      identifier: 'FOO',
      uuid: '5AFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF'
    });
});

b2.addEventListener('click', function(e) {

	// stop everything
	TiBeacons.stopMonitoringAllRegions();
    TiBeacons.removeEventListener("enteredRegion", entered);
    TiBeacons.removeEventListener("exitedRegion", exited);

});

win.setLayout('vertical');
win.add(b1);
win.add(b2);

win.open();
```

## Author

![James Falkner Logo](https://cdn.lfrs.sl/www.liferay.com/image/user_male_portrait?img_id=6182018&t=1402762276765)

* James Falkner (Liferay Community Manager)
* `james.falkner@liferay.com`
* [`@schtool`](http://twitter.com/schtool)

## License

Copyright (c) 2015, Liferay Inc. All rights reserved.

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

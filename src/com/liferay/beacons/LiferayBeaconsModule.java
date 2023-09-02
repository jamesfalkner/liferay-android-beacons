/**
 * Copyright 2015 Liferay, Inc. All rights reserved.
 * http://www.liferay.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author James Falkner
 */

package com.liferay.beacons;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import com.radiusnetworks.ibeacon.*;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Events: enteredRegion, exitedRegion, determinedRegionState, beaconProximity, success, error
 */
@Kroll.module(name="LiferayBeacons", id="com.liferay.beacons")
public class LiferayBeaconsModule extends KrollModule implements IBeaconConsumer
{

	private static IBeaconManager iBeaconManager;

	// Standard Debugging variables
	private static final String TAG = "LiferayBeaconsModule";

	private boolean autoRange = true;
	private boolean ready = false;
	private BeaconTransmitter beaconTransmitter;

	public LiferayBeaconsModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "onAppCreate: Liferay Android Beacons 0.4");

		iBeaconManager = IBeaconManager.getInstanceForApplication(app);

		// set some less battery-intensive settings compared to the defaults
		iBeaconManager.setForegroundScanPeriod(1200);
		iBeaconManager.setForegroundBetweenScanPeriod(2300);
		iBeaconManager.setBackgroundScanPeriod(10000);
		iBeaconManager.setBackgroundBetweenScanPeriod(60 * 1000);

		// to see debugging from the Radius networks lib, set this to true
		IBeaconManager.LOG_DEBUG = false;
	}

	/**
	 * See if Bluetooth 4.0 & LE is available on device
	 *
	 * @return true if iBeacons can be used, false otherwise
	 */
	@Kroll.method
	public boolean checkAvailability() {
		try {
			return iBeaconManager.checkAvailability();
		} catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * See if Module is Ready
	 *
	 * @return true/false
	 */
	@Kroll.method
	public boolean isReady() {
		return this.ready;
	}

	/**
	 * Binds the activity to the Beacon Service
	 */
	@Kroll.method
	public void bindBeaconService() {
		Log.d(TAG, "bindService");
		iBeaconManager.bind(this);
	}

	/**
	 * Unbinds the activity to the Beacon Service
	 */
	@Kroll.method
	public void unbindBeaconService() {
		Log.d(TAG, "unbindService");
		iBeaconManager.unBind(this);
	}

	/**
	 * Throttles down iBeacon library when app placed in background (but you have to
	 * detect this yourself, this module does not know when apps are put in background).
	 *
	 * @param flag Whether to enable background mode or not.
	 */
	@Kroll.method
	public void setBackgroundMode(boolean flag)
	{
		Log.d(TAG, "setBackgroundMode: " + flag);

		if (!checkAvailability()) {
			Log.d(TAG, "Bluetooth LE not available or no permissions on this device");
			return;
		}
		iBeaconManager.setBackgroundMode(this, flag);


	}

	/**
	 * Turns on auto ranging. When auto ranging is on, upon entering a region, this
	 * module will automatically begin ranging for beacons within that region, and
	 * stop ranging for beacons when the region is exited. Note ranging requires more
	 * battery power so care should be taken with this setting.
	 */
	@Kroll.method
	public void enableAutoRanging()
	{
		setAutoRange(true);
	}

	/**
	 * Turns off auto ranging. See description of enableAutoRanging for more details.
	 *
	 * @see #enableAutoRanging()
	 */
	@Kroll.method
	public void disableAutoRanging()
	{
		setAutoRange(false);
	}

	/**
	 * Turns auto ranging on or off. See description of enableAutoRanging for more details.
	 *
	 * @param autoRange if true, turns on auto ranging. Otherwise, turns it off.
	 *
	 * @see #enableAutoRanging()
	 *
	 */
	@Kroll.method
	public void setAutoRange(boolean autoRange)
	{
		Log.d(TAG, "setAutoRange: " + autoRange);
		this.autoRange = autoRange;

	}

	/**
	 * Set the scan periods for the bluetooth scanner.
	 *
	 * @param scanPeriods the scan periods.
	 */
	@Kroll.method
	public void setScanPeriods(Object scanPeriods)
	{

		Log.d(TAG, "setScanPeriods: " + scanPeriods);

		HashMap<String, Object> dict = (HashMap<String, Object>)scanPeriods;

		int foregroundScanPeriod = TiConvert.toInt(dict, "foregroundScanPeriod");
		int foregroundBetweenScanPeriod = TiConvert.toInt(dict, "foregroundBetweenScanPeriod");
		int backgroundScanPeriod = TiConvert.toInt(dict, "backgroundScanPeriod");
		int backgroundBetweenScanPeriod = TiConvert.toInt(dict, "backgroundBetweenScanPeriod");

		iBeaconManager.setForegroundScanPeriod(foregroundScanPeriod);
		iBeaconManager.setForegroundBetweenScanPeriod(foregroundBetweenScanPeriod);
		iBeaconManager.setBackgroundScanPeriod(backgroundScanPeriod);
		iBeaconManager.setBackgroundBetweenScanPeriod(backgroundBetweenScanPeriod);
	}

	/**
	 * Start monitoring a region.
	 * @param region the region to monitor, expected to be a property dictionary from javascript code.
	 */
	@Kroll.method
	public void startMonitoringForRegion(Object region)
	{
		Log.d(TAG, "startMonitoringForRegion: " + region);

		if (!checkAvailability()) {
			Log.d(TAG, "Bluetooth LE not available or no permissions on this device");
			return;
		}
		try {
			HashMap<String, Object> dict = (HashMap<String, Object>)region;

			String identifier = TiConvert.toString(dict, "identifier");
			String uuid = TiConvert.toString(dict, "uuid").toLowerCase();
			Integer major = (dict.get("major") != null) ? TiConvert.toInt(dict, "major") : null;
			Integer minor = (dict.get("minor") != null) ? TiConvert.toInt(dict, "minor") : null;

			Region r = new Region(identifier, uuid, major, minor);

			Log.d(TAG, "Beginning to monitor region " + r);
			iBeaconManager.startMonitoringBeaconsInRegion(r);
		} catch (RemoteException ex) {
			Log.e(TAG, "Cannot start monitoring region " + TiConvert.toString(region, "identifier"), ex);
		}
	}


	/**
	 * Compatibility method for popular iOS FOSS iBeacon library.
	 *
	 * @see #startRangingForRegion(Object)
	 *
	 * @param region the region to range, expected to be a property dictionary from javascript code.
	 */
	@Kroll.method
	public void startRangingForBeacons(Object region) {
		startRangingForRegion(region);
	}

	/**
	 * Start ranging a region. You can only range regions into which you have entered.
	 *
	 * @param region the region to range, expected to be a property dictionary from javascript code.
	 */
	@Kroll.method
	public void startRangingForRegion(Object region)
	{
		Log.d(TAG, "startRangingForRegion: " + region);

		if (!checkAvailability()) {
			Log.d(TAG, "Bluetooth LE not available or no permissions on this device");
			return;
		}
		try {
			HashMap<String, Object> dict = (HashMap<String, Object>)region;

			String identifier = TiConvert.toString(dict, "identifier");
			String uuid = TiConvert.toString(dict, "uuid").toLowerCase();
			Integer major = (dict.get("major") != null) ? TiConvert.toInt(dict, "major") : null;
			Integer minor = (dict.get("minor") != null) ? TiConvert.toInt(dict, "minor") : null;

			Region r = new Region(identifier, uuid, major, minor);

			Log.d(TAG, "Beginning to monitor region " + r);
			iBeaconManager.startRangingBeaconsInRegion(r);
		} catch (RemoteException ex) {
			Log.e(TAG, "Cannot start ranging region " + TiConvert.toString(region, "identifier"), ex);
		}
	}


	/**
	 * Stop monitoring everything.
	 */
	@Kroll.method
	public void stopMonitoringAllRegions()
	{

		Log.d(TAG, "stopMonitoringAllRegions");

		for (Region r : iBeaconManager.getMonitoredRegions()) {
			try {
				iBeaconManager.stopMonitoringBeaconsInRegion(r);
				Log.d(TAG, "Stopped monitoring region " + r);
			} catch (RemoteException ex) {
				Log.e(TAG, "Cannot stop monitoring region " + r.getUniqueId(), ex);
			}
		}

	}

	/**
	 * Stop ranging for everything.
	 */
	@Kroll.method
	public void stopRangingForAllBeacons()
	{

		Log.d(TAG, "stopRangingForAllBeacons");

		for (Region r : iBeaconManager.getRangedRegions()) {
			try {
				iBeaconManager.stopRangingBeaconsInRegion(r);
				Log.d(TAG, "Stopped ranging region " + r);
			} catch (RemoteException ex) {
				Log.e(TAG, "Cannot stop ranging region " + r.getUniqueId(), ex);
			}
		}
	}


	@Override
	public void onStart(Activity activity)
	{
		// This method is called when the module is loaded and the root context is started

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] start");
		iBeaconManager.bind(this);

		super.onStart(activity);
	}

	@Override
	public void onStop(Activity activity)
	{
		// This method is called when the root context is stopped

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] stop");

		if (!iBeaconManager.isBound(this)) {
			iBeaconManager.bind(this);
		}
		super.onStop(activity);
	}

	@Override
	public void onPause(Activity activity)
	{
		// This method is called when the root context is being suspended

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] pause");
		if (!iBeaconManager.isBound(this)) {
			iBeaconManager.bind(this);
		}

		super.onPause(activity);
	}

	@Override
	public void onResume(Activity activity)
	{
		// This method is called when the root context is being resumed

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] resume");
		if (!iBeaconManager.isBound(this)) {
			iBeaconManager.bind(this);
		}

		super.onResume(activity);
	}

	@Override
	public void onDestroy(Activity activity)
	{
		// This method is called when the root context is being destroyed

		Log.d(TAG, "[MODULE LIFECYCLE EVENT] destroy");
		iBeaconManager.unBind(this);

		super.onDestroy(activity);
	}
	
	public void onIBeaconServiceConnect() {
		KrollDict e = new KrollDict();
		e.put("message", "success");
		fireEvent("serviceBound", e);

		Log.d(TAG, "onIBeaconServiceConnect");
		this.ready = true; //so we know the module is ready to setup event listeners
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {

			public void didEnterRegion(Region region) {

				Log.d(TAG, "Entered region: " + region);

				try {
					if (autoRange) {
						Log.d(TAG, "Beginning to autoRange region " + region);
						iBeaconManager.startRangingBeaconsInRegion(region);
					}
					KrollDict e = new KrollDict();
					e.put("identifier", region.getUniqueId());
					fireEvent("enteredRegion", e);
				} catch (RemoteException ex) {
					Log.e(TAG, "Cannot turn on ranging for region " + region.getUniqueId(), ex);
				}
			}

			public void didExitRegion(Region region) {

				Log.d(TAG, "Exited region: " + region);

				try {
					iBeaconManager.stopRangingBeaconsInRegion(region);
					KrollDict e = new KrollDict();
					e.put("identifier", region.getUniqueId());
					fireEvent("exitedRegion", e);
				} catch (RemoteException ex) {
					Log.e(TAG, "Cannot turn off ranging for region " + region.getUniqueId(), ex);
				}
			}

			public void didDetermineStateForRegion(int state, Region region) {
				if (state == INSIDE) {
					try {
						if (autoRange) {
							Log.d(TAG, "Beginning to autoRange region " + region);
							iBeaconManager.startRangingBeaconsInRegion(region);
						}
						KrollDict e = new KrollDict();
						e.put("identifier", region.getUniqueId());
						e.put("regionState", "inside");
						fireEvent("determinedRegionState", e);
					} catch (RemoteException e) {
						Log.e(TAG, "Cannot turn on ranging for region during didDetermineState" + region);
					}
				} else if (state == OUTSIDE) {
					try {
						iBeaconManager.stopRangingBeaconsInRegion(region);
						KrollDict e = new KrollDict();
						e.put("identifier", region.getUniqueId());
						e.put("regionState", "outside");
						fireEvent("determinedRegionState", e);
					} catch (RemoteException e) {
						Log.e(TAG, "Cannot turn off ranging for region during didDetermineState" + region);
					}
				} else {
					Log.d(TAG, "Unknown region state: " + state + " for region: " + region);
				}

			}
		});

		iBeaconManager.setRangeNotifier(new RangeNotifier() {
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {

				List<KrollDict> finalBeacons = new ArrayList<KrollDict>(iBeacons.size());

				for (IBeacon beacon : iBeacons) {
					KrollDict beaconDict = new KrollDict();
					beaconDict.put("identifier", region.getUniqueId());
					beaconDict.put("uuid", beacon.getProximityUuid());
					beaconDict.put("major", beacon.getMajor());
					beaconDict.put("minor", beacon.getMinor());
					beaconDict.put("proximity", getProximityName(beacon.getProximity()));
					beaconDict.put("accuracy", beacon.getAccuracy());
					beaconDict.put("rssi", beacon.getRssi());
					beaconDict.put("power", beacon.getTxPower());
					finalBeacons.add(beaconDict);

					fireEvent("beaconProximity", beaconDict);
				}

				KrollDict e = new KrollDict();
				e.put("identifier", region.getUniqueId());
				e.put("beacons", finalBeacons.toArray());
				fireEvent("beaconRanges", e);
			}

		});

	}

	public static String getProximityName(int p) {
		switch (p) {
			case IBeacon.PROXIMITY_FAR:
				return "far";
			case IBeacon.PROXIMITY_IMMEDIATE:
				return "immediate";
			case IBeacon.PROXIMITY_NEAR:
				return "near";
			default:
				return "unknown";
		}
	}


	// methods to bind and unbind

	public Context getApplicationContext() {
		return super.getActivity().getApplicationContext();
	}

	public void unbindService(ServiceConnection serviceConnection) {
		Log.d(TAG, "unbindService");
		super.getActivity().unbindService(serviceConnection);
	}

	public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
		Log.d(TAG, "bindService");
		return super.getActivity().bindService(intent, serviceConnection, i);
	}

	@SuppressLint("NewApi")
	@Kroll.method
	public void startAdvertise(KrollDict kd) {
		String id1 = TiConvert.toString(kd.get("id1"), "");
		String id2 = TiConvert.toString(kd.get("id2"), "");
		String id3 = TiConvert.toString(kd.get("id3"), "");
		String beaconLayout = TiConvert.toString(kd.get("beaconLayout"), "");
		int manu = TiConvert.toInt(kd.get("manufacturer"), 0);
		int txPower = TiConvert.toInt(kd.get("txPower"), 0);

		Beacon beacon = new Beacon.Builder()
				.setId1(id1)
				.setId2(id2)
				.setId3(id3)
				.setManufacturer(manu)
				.setTxPower(txPower)
				.setDataFields(Arrays.asList(new Long[] {0l}))
				.build();
		BeaconParser beaconParser = new BeaconParser().setBeaconLayout(beaconLayout);
		beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
		beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
			@Override
			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
				super.onStartSuccess(settingsInEffect);
				KrollDict kdSuccess = new KrollDict();
				fireEvent("success", kdSuccess);
			}

			@Override
			public void onStartFailure(int errorCode) {
				super.onStartFailure(errorCode);
				KrollDict kdError = new KrollDict();
				kdError.put("errorCode", errorCode);
				fireEvent("error", kdError);
			}
		});
	}

	@Kroll.method
	public void stopAdvertise() {
		if (beaconTransmitter != null) {
			beaconTransmitter.stopAdvertising();
			beaconTransmitter = null;
		}
	}
}


//
// Copyright 2014 Liferay, Inc. All rights reserved.
// http://www.liferay.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

var win = Ti.UI.createWindow({
	backgroundColor:'white'
});
var label = Ti.UI.createLabel();
win.add(label);
win.open();

if (Ti.Platform.name == "android") {
    var mod = require('com.liferay.beacons');
    label.text = "module is => " + mod + "and checkAvailability says: " + mod.checkAvailability();
} else {
    label.text = "liferay.beacons not supported on " + Ti.Platform.name;
}
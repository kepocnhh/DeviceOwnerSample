# DeviceOwnerSample
Device owner sample app.

---

### Set device owner

```
$ adb shell dpm set-device-owner test.android.downer.debug/test.android.downer.MainDeviceAdminReceiver
```

#### List owners

```
$ adb shell dpm list-owners
```

#### Unset device owner

```
$ adb shell dpm remove-active-admin test.android.downer.debug/test.android.downer.MainDeviceAdminReceiver
```

#### Force stop

```
$ adb shell am force-stop test.android.downer.debug
```

---

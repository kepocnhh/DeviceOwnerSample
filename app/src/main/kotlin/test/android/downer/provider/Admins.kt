package test.android.downer.provider

import kotlinx.coroutines.flow.StateFlow

internal interface Admins {
    class Versions(
        val api: String,
        val sdk: String,
        val firmware: String,
        val mcu: String?,
    )

    class DeviceInfo(
        val serialNumber: String,
        val macEthernet: String,
    )

    val owners: StateFlow<Boolean>

    fun update(isDeviceOwner: Boolean)
    fun getVersions(): Versions
    fun getDeviceInfo(): DeviceInfo
}

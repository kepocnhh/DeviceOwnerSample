package test.android.downer.provider

import kotlinx.coroutines.flow.StateFlow

internal interface Admins {
    class Versions(
        val api: String,
        val sdk: String,
        val firmware: String,
        val mcu: String?,
    )

    val owners: StateFlow<Boolean>

    fun update(isDeviceOwner: Boolean)
    fun getVersions(): Versions
}
